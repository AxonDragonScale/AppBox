package com.axondragonscale.logviewer.logcat

import android.app.ActivityManager
import android.content.Context
import com.axondragonscale.logviewer.model.LogId
import com.axondragonscale.logviewer.model.LogLevel
import com.axondragonscale.logviewer.model.LogMeta
import com.axondragonscale.logviewer.model.LogRecord
import com.axondragonscale.logviewer.source.LogCapabilities
import com.axondragonscale.logviewer.source.LogQuery
import com.axondragonscale.logviewer.source.LogSource
import com.axondragonscale.logviewer.source.LogStream
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import java.io.File
import kotlin.time.Clock

/** Created by Ronak Harkhani on 11/22/26 */

private val alwaysLoaded: StateFlow<Boolean> = MutableStateFlow(true).asStateFlow()
private val neverOlder: StateFlow<Boolean> = MutableStateFlow(false).asStateFlow()

/**
 * Reads device logs as the calling UID and parses them into [LogRecord].
 *
 * One reader lives for the source's lifetime: a filter change only rebuilds the query view. That
 * matters on platforms that prompt for log access per read — restarting `logcat` on every keystroke
 * would prompt every time.
 *
 * It runs `logcat -v threadtime,year` so every record carries a full date, and asks for all buffers
 * on the first try, falling back to the default buffers on a device that refuses `-b all`. The
 * format never leaves this class.
 *
 * History is the live buffer plus whatever was there when the source opened, so
 * [LogCapabilities.history] is false and the viewer stops asking for older pages.
 */
class LogcatSource(context: Context) : LogSource {

    private val resolver = PackageResolver(context.applicationContext)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val buffer = ArrayDeque<LogRecord>()
    private val records = MutableStateFlow(emptyList<LogRecord>())
    private val blocked = MutableStateFlow<String?>(null)
    private var sequence = 0L
    private var process: java.lang.Process? = null
    private var lastFlush = 0L
    private var started = false

    override val capabilities: LogCapabilities = LogCapabilities(
        live = true,
        history = false,
        pid = true,
        tid = true,
        packageName = true,
        sessions = false,
        clear = false,
    )

    override fun open(query: LogQuery): LogStream {
        start()
        return View(query)
    }

    /** Stops the reader; only a host that wants the process gone should call this. */
    fun close() {
        scope.cancel()
        process?.destroy()
        process = null
    }

    private fun start() {
        if (started) return
        started = true
        // The read loop blocks on the pipe and never suspends, so it gets the IO pool to itself
        // instead of starving the views collected on [scope].
        scope.launch(Dispatchers.IO.limitedParallelism(1)) { read(listOf("-b", "all")) }
        scope.launch {
            delay(NOTICE_DELAY_MS)
            // A silent reader on a gated platform means the system is withholding the logs.
            if (buffer.isEmpty()) {
                blocked.value = "Device logs are blocked by the system. Allow this app when it asks, then reopen."
            }
        }
    }

    private inner class View(private val query: LogQuery) : LogStream {

        override val entries: StateFlow<List<LogRecord>> = records
            .map { all -> all.filter(query::matches) }
            .stateIn(scope, SharingStarted.WhileSubscribed(0), emptyList())

        override val isLoaded: StateFlow<Boolean> = alwaysLoaded

        override val hasOlder: StateFlow<Boolean> = neverOlder

        override val notice: Flow<String?> = blocked.asStateFlow()

        override suspend fun loadOlder() = Unit

        override suspend fun clear() = Unit

        override fun close() = Unit
    }

    private suspend fun read(bufferArgs: List<String>) {
        var produced = false
        val started = startProcess(bufferArgs) ?: return
        process = started

        try {
            started.inputStream.bufferedReader().use { input ->
                while (scope.isActive) {
                    val line = input.readLine() ?: break
                    produced = true
                    append(line)
                }
            }
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            // Closing the process under the loop is normal; anything else ends the reader for good.
            if (scope.isActive) {
                blocked.value = "The log reader stopped. Reopen Logcat to start it again."
            }
        } finally {
            started.destroy()
            publish()
        }

        // Some devices refuse `-b all` for an app UID; the default buffers still hold the records.
        if (!produced && bufferArgs.isNotEmpty() && scope.isActive) read(emptyList())
    }

    private fun startProcess(bufferArgs: List<String>): java.lang.Process? = runCatching {
        ProcessBuilder(listOf("logcat", "-v", "threadtime,year") + bufferArgs).start()
    }.getOrNull()

    private fun append(line: String) {
        when {
            line.startsWith("---------") -> Unit

            isRecord(line) -> {
                parse(line)?.let { record ->
                    blocked.value = null
                    buffer.addLast(record)
                    if (buffer.size > BUFFER_LIMIT) buffer.removeFirst()
                }
            }

            // A continuation (stack frame, wrapped message) belongs to the record above it.
            line.isNotBlank() && buffer.isNotEmpty() -> {
                val last = buffer.removeLast()
                buffer.addLast(last.copy(message = last.message + "\n" + line.trimEnd()))
            }
        }
        flushIfDue()
    }

    private fun flushIfDue() {
        val now = Clock.System.now().toEpochMilliseconds()
        if (now - lastFlush >= FLUSH_INTERVAL_MS) {
            lastFlush = now
            publish()
        }
    }

    private fun publish() {
        // Newest first: the viewer's contract. A materialised list, because a reversed view would
        // keep changing under the flow without ever emitting.
        records.value = buffer.toList().asReversed()
    }

    private fun isRecord(line: String): Boolean = line.length > 32 && line[4] == '-' && line[7] == '-'

    private fun parse(line: String): LogRecord? {
        val values = LINE.matchEntire(line)?.groupValues ?: return null
        val time = runCatching {
            LocalDateTime(
                year = values[1].toInt(),
                monthNumber = values[2].toInt(),
                dayOfMonth = values[3].toInt(),
                hour = values[4].toInt(),
                minute = values[5].toInt(),
                second = values[6].toInt(),
                nanosecond = values[7].toInt() * 1_000_000,
            ).toInstant(TimeZone.currentSystemDefault())
        }.getOrNull() ?: return null

        val letter = values[10].first()
        val pid = values[8].toIntOrNull() ?: -1

        return LogRecord(
            id = LogId("${sequence++}"),
            time = time,
            level = when (letter) {
                'V' -> LogLevel.TRACE
                'D' -> LogLevel.DEBUG
                'I' -> LogLevel.INFO
                'W' -> LogLevel.WARN
                'E' -> LogLevel.ERROR
                // `LINE` admits F and A; logcat's assert is what FATAL means here.
                else -> LogLevel.FATAL
            },
            tag = values[11].trim(),
            message = values[12],
            meta = LogMeta(
                pid = pid,
                tid = values[9].toIntOrNull(),
                packageName = resolver.packageFor(pid),
            ),
        )
    }

    private companion object {
        const val BUFFER_LIMIT = 5_000
        const val FLUSH_INTERVAL_MS = 120L
        const val NOTICE_DELAY_MS = 3_000L

        val LINE = Regex(
            """^(\d{4})-(\d{2})-(\d{2})\s+(\d{2}):(\d{2}):(\d{2})\.(\d{3})\s+""" +
                """(\d+)\s+(\d+)\s+([VDIWEFA])\s+([^:]*?)\s*:\s?(.*)$"""
        )
    }
}

/** Best effort: the platform hides other processes, `/proc/<pid>/cmdline` often still answers. */
private class PackageResolver(private val context: Context) {

    private val activityManager = context.getSystemService(ActivityManager::class.java)

    /** Bounded: a PID can be reused, and a row older than the buffer is never asked for again. */
    private val cache = object : LinkedHashMap<Int, String?>(64, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<Int, String?>): Boolean =
            size > CACHE_LIMIT
    }

    fun packageFor(pid: Int): String? {
        if (pid < 0) return null
        synchronized(cache) {
            if (cache.containsKey(pid)) return cache[pid]
            val name = resolve(pid)
            cache[pid] = name
            return name
        }
    }

    private fun resolve(pid: Int): String? {
        if (pid == android.os.Process.myPid()) return context.packageName

        val fromProcesses = runCatching {
            activityManager?.runningAppProcesses
                ?.firstOrNull { it.pid == pid }
                ?.processName
                ?.substringBefore(':')
        }.getOrNull()

        return fromProcesses?.takeIf { it.isNotBlank() } ?: runCatching {
            File("/proc/$pid/cmdline").readText().trim().substringBefore(':').takeIf { it.isNotBlank() }
        }.getOrNull()
    }

    private companion object {
        const val CACHE_LIMIT = 512
    }
}
