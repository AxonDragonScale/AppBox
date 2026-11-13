package com.axondragonscale.logcatviewer.stream

import android.util.Log
import com.axondragonscale.commons.catching
import com.axondragonscale.logcatviewer.data.ILogcatProcess
import com.axondragonscale.logcatviewer.data.ILogcatSource
import com.axondragonscale.logcatviewer.model.LogBuffer
import com.axondragonscale.logcatviewer.model.LogEntry
import com.axondragonscale.logcatviewer.model.LogLevel
import java.util.ArrayDeque
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** Created by Ronak on 09/11/26 */

private const val TAG = "LogcatStreamer"
private const val ANDROID_RUNTIME = "AndroidRuntime"
private const val FATAL_HEADER = "FATAL EXCEPTION"

/** Bounds the view, so a stream left open all day cannot grow without limit. */
private const val MAX_ENTRIES = 5_000

/** One entry's text cap; a trace longer than this stops being readable in place. */
private const val MAX_MESSAGE_CHARS = 16_384

/** Batches publications, so the list rebuilds at most ~8 times a second. */
private const val PUBLISH_INTERVAL_MS = 120L
private const val RESTART_DELAY_MS = 1_000L
private const val MAX_RESTARTS = 3

private val TRACE_PREFIXES = listOf(
    "at ",
    "Caused by:",
    "Suppressed:",
    "...",
    "Process:",
    "PID:",
    "Thread:",
    "java.",
)

/**
 * The viewer's one reader. It parses, merges traces and batches, so the UI is not rebuilt per line.
 * The process comes from the host's [ILogcatSource].
 */
internal class LogcatStreamer(
    private val source: ILogcatSource,
) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val lock = Any()

    private val visible = ArrayDeque<LogEntry>()
    private val pending = ArrayDeque<LogEntry>()
    private var nextId = 0L
    private var process: ILogcatProcess? = null

    /** The buffer the shown lines belong to; a different buffer starts a fresh view. */
    private var sessionBuffer: LogBuffer? = null

    /** Owns the current reader. A stale screen's [stop] is ignored, so it cannot kill a newer one. */
    private var owner: LogSession? = null
    private var restarts = 0

    /** `MM-DD HH:MM:SS.mmm` of the newest accepted record; the cursor a resume picks up after. */
    private var lastKey: String? = null

    /** Set while a resume replays the buffer: seeds at or before this are already on screen. */
    private var replayAfter: String? = null
    private var readJob: Job? = null
    private var errorJob: Job? = null
    private var publishJob: Job? = null

    private val _entries = MutableStateFlow<List<LogEntry>>(emptyList())
    val entries: StateFlow<List<LogEntry>> = _entries.asStateFlow()

    private val _isPaused = MutableStateFlow(false)
    val isPaused: StateFlow<Boolean> = _isPaused.asStateFlow()

    private val _pendingCount = MutableStateFlow(0)
    val pendingCount: StateFlow<Int> = _pendingCount.asStateFlow()

    /**
     * Opens a reader and returns the session that owns it.
     *
     * The shown lines outlive the process: a stop keeps them, and starting the same buffer again
     * resumes after the newest one. That matters because the platform stops delivering records to an
     * app's `logcat` child once the app is backgrounded, so the viewer stops the child there and
     * reopens it on return, filling the gap from the buffer without repeating what is on screen.
     * A different buffer is a new view and starts empty.
     */
    fun start(buffer: LogBuffer): LogSession = synchronized(lock) {
        if (process != null && buffer == sessionBuffer) return@synchronized owner ?: LogSession()
        restarts = 0
        val session = if (buffer == sessionBuffer) owner ?: LogSession() else LogSession()
        open(buffer, session, reset = buffer != sessionBuffer)
        session
    }

    fun stop(owner: LogSession) {
        synchronized(lock) {
            if (owner !== this.owner) return
            this.owner = null
            teardown()
        }
    }

    private fun open(buffer: LogBuffer, session: LogSession, reset: Boolean) {
        val cursor = lastKey
        teardown()
        owner = session
        if (reset) {
            visible.clear()
            pending.clear()
            replayAfter = null
            lastKey = null
            sessionBuffer = buffer
            _isPaused.value = false
            _entries.value = emptyList()
            _pendingCount.value = 0
        } else {
            replayAfter = cursor
        }

        val started = TAG.catching { source.open(buffer) }.getSuccessResultOrNull() ?: return

        process = started
        errorJob = scope.launch { drainErrors(started) }
        readJob = scope.launch {
            val lines = read(started)
            val died = synchronized(lock) {
                if (process !== started) return@synchronized false
                process = null
                true
            }
            if (died) scheduleRestart(session, buffer, lines)
        }
        publishJob = scope.launch {
            while (isActive) {
                delay(PUBLISH_INTERVAL_MS)
                publish()
            }
        }
    }

    private fun teardown() {
        readJob?.cancel()
        errorJob?.cancel()
        publishJob?.cancel()
        readJob = null
        errorJob = null
        publishJob = null
        process?.close()
        process = null
    }

    /**
     * A source follows the buffer, so an exit it did not choose means the stream broke. One restart
     * keeps the tail alive; a reader that produced nothing is left alone so a failure does not spin.
     */
    private fun scheduleRestart(session: LogSession, buffer: LogBuffer, lines: Int) {
        val shouldRestart = synchronized(lock) {
            if (lines == 0 || restarts >= MAX_RESTARTS || session !== owner) {
                false
            } else {
                restarts++
                true
            }
        }
        if (!shouldRestart) return
        scope.launch {
            delay(RESTART_DELAY_MS)
            synchronized(lock) {
                if (session === owner && process == null) open(buffer, session, reset = false)
            }
        }
    }

    fun clear() {
        synchronized(lock) {
            visible.clear()
            pending.clear()
            publishLocked()
        }
    }

    fun setPaused(value: Boolean) {
        synchronized(lock) {
            if (_isPaused.value == value) return
            _isPaused.value = value
            if (!value) {
                visible.addAll(pending)
                pending.clear()
                trim(visible)
            }
            publishLocked()
        }
    }

    private suspend fun read(source: ILogcatProcess): Int = withContext(Dispatchers.IO) {
        val reader = source.input.bufferedReader()
        var lines = 0
        TAG.catching {
            while (currentCoroutineContext().isActive) {
                val line = reader.readLine() ?: break
                lines++
                TAG.catching { synchronized(lock) { accept(line) } }
            }
        }
        lines
    }

    /** `logcat` writes failures to stderr; leaving it unread eventually blocks the process. */
    private suspend fun drainErrors(source: ILogcatProcess) = withContext(Dispatchers.IO) {
        TAG.catching {
            source.error.bufferedReader().forEachLine { line ->
                if (line.isNotBlank()) Log.d(TAG, "logcat: $line")
            }
        }
    }

    private fun accept(line: String) {
        // `--------- beginning of main` separates buffers; it is not a record and belongs nowhere.
        if (line.startsWith("---------")) return
        val match = HEADER.matchEntire(line)
        if (match == null) {
            appendToLast(line)
            return
        }
        val (date, time, pid, tid, letter, tag, message) = match.destructured
        val key = "$date $time"
        replayAfter?.let { cursor ->
            if (!isAfter(key, cursor)) return
            replayAfter = null
        }
        val level = LogLevel.from(letter[0]) ?: return
        lastKey = key
        val entry = LogEntry(
            id = nextId++,
            time = time,
            pid = pid.toIntOrNull() ?: 0,
            tid = tid.toIntOrNull() ?: 0,
            level = level,
            tag = tag.trim(),
            message = message,
        )
        val queue = queue()
        val previous = queue.lastOrNull()
        if (previous != null && mergesInto(previous, entry)) {
            appendToLast(entry.message)
            return
        }
        queue.add(entry)
        trim(queue)
    }

    private fun queue(): ArrayDeque<LogEntry> = if (_isPaused.value) pending else visible

    private fun trim(queue: ArrayDeque<LogEntry>) {
        while (queue.size > MAX_ENTRIES) queue.removeFirst()
    }

    /**
     * Stack frames and crash blocks arrive as separate header lines; they belong to the entry above.
     * Same pid, tid and tag keeps unrelated apps and tags apart.
     */
    private fun mergesInto(previous: LogEntry, candidate: LogEntry): Boolean {
        if (previous.pid != candidate.pid || previous.tid != candidate.tid) return false
        if (previous.tag != candidate.tag) return false
        val message = candidate.message
        val trimmed = message.trimStart()
        if (message != trimmed || TRACE_PREFIXES.any { trimmed.startsWith(it) }) return true
        // An app's own exception class has no known prefix; it still belongs to the crash above.
        return previous.tag == ANDROID_RUNTIME && previous.firstLine.startsWith(FATAL_HEADER)
    }

    private fun appendToLast(text: String) {
        if (replayAfter != null) return
        val queue = queue()
        val previous = queue.lastOrNull() ?: return
        queue.removeLast()
        queue.add(previous.copy(message = (previous.message + "\n" + text).take(MAX_MESSAGE_CHARS)))
    }

    /**
     * Orders `MM-DD HH:MM:SS.mmm` stamps, which sort as written within a year. A buffer that wrapped
     * across New Year compares as older, so the view resumes on the next reopen instead.
     */
    private fun isAfter(key: String, cursor: String): Boolean = key > cursor

    private fun publish() {
        synchronized(lock) { publishLocked() }
    }

    private fun publishLocked() {
        _entries.value = visible.toList()
        _pendingCount.value = pending.size
    }

    private companion object {
        /** The level letters come from [LogLevel], so the parser cannot accept a level it cannot map. */
        val LEVEL_LETTERS = LogLevel.entries.joinToString("") { it.letter.toString() }

        /** `MM-DD HH:MM:SS.mmm  PID  TID L TAG: message`; everything else continues the last line. */
        val HEADER = Regex(
            """^(\d{2}-\d{2})\s+(\d{2}:\d{2}:\d{2}\.\d{3})\s+(\d+)\s+(\d+)\s+([$LEVEL_LETTERS])\s+(.*?)\s*:\s?(.*)$""",
        )
    }
}

/** Identity of one open reader; [LogcatStreamer.stop] ignores a token that is not the current owner. */
internal class LogSession
