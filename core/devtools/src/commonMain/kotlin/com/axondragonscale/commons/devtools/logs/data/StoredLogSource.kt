package com.axondragonscale.commons.devtools.logs.data

import com.axondragonscale.commons.LogPriority
import com.axondragonscale.logviewer.model.LogId
import com.axondragonscale.logviewer.model.LogLevel
import com.axondragonscale.logviewer.model.LogMeta
import com.axondragonscale.logviewer.model.LogRecord
import com.axondragonscale.logviewer.source.LogCapabilities
import com.axondragonscale.logviewer.source.LogQuery
import com.axondragonscale.logviewer.source.LogSource
import com.axondragonscale.logviewer.source.LogStream
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.time.Instant

/** Created by Ronak Harkhani on 11/22/26 */

private const val PAGE_SIZE = 200
private const val POLL_INTERVAL_MS = 2_000L

/**
 * The persisted dev log store as a viewer source: keyset pages for history, a poll for the live
 * tail, and a clear action over the database itself.
 */
internal class StoredLogSource(private val dao: () -> DevLogDao?) : LogSource {

    override val capabilities: LogCapabilities = LogCapabilities(
        live = true,
        history = true,
        pid = false,
        tid = false,
        packageName = false,
        sessions = true,
        clear = true,
    )

    override suspend fun tags(): List<String> =
        runCatching { dao()?.tags().orEmpty() }.getOrDefault(emptyList())

    override fun open(query: LogQuery): LogStream = Stream(dao, query)
}

private class Stream(
    private val dao: () -> DevLogDao?,
    private val query: LogQuery,
) : LogStream {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val loaded = MutableStateFlow(emptyList<LogRecord>())
    override val entries: StateFlow<List<LogRecord>> = loaded.asStateFlow()

    private val ready = MutableStateFlow(false)
    override val isLoaded: StateFlow<Boolean> = ready.asStateFlow()

    private val older = MutableStateFlow(false)
    override val hasOlder: StateFlow<Boolean> = older.asStateFlow()

    private var loading = false

    init {
        scope.launch {
            loadFirst()
            poll()
        }
    }

    override suspend fun loadOlder() {
        val before = loaded.value.lastOrNull()?.id?.value?.toLongOrNull() ?: return
        if (loading || !older.value) return
        loading = true
        val rows = runCatching { page(beforeId = before) }.getOrDefault(emptyList())
        if (rows.isNotEmpty()) {
            val known = loaded.value.mapTo(mutableSetOf()) { it.id }
            loaded.value = loaded.value + rows.filter { it.id !in known }
        }
        older.value = rows.size >= PAGE_SIZE
        loading = false
    }

    override suspend fun clear() {
        runCatching { dao()?.clear() }
        loaded.value = emptyList()
        older.value = false
        loadFirst()
    }

    override fun close() {
        scope.cancel()
    }

    private suspend fun loadFirst() {
        val rows = runCatching { page(beforeId = Long.MAX_VALUE) }.getOrDefault(emptyList())
        loaded.value = rows
        older.value = rows.size >= PAGE_SIZE
        ready.value = true
    }

    /** The newest page plus whatever history is already loaded, newest first. */
    private suspend fun poll() {
        while (scope.isActive) {
            delay(POLL_INTERVAL_MS)
            val rows = runCatching { page(beforeId = Long.MAX_VALUE) }.getOrDefault(emptyList())
            if (rows.isEmpty()) continue
            val merged = (rows + loaded.value).distinctBy { it.id }
            if (merged.size != loaded.value.size || merged.firstOrNull()?.id != loaded.value.firstOrNull()?.id) {
                loaded.value = merged
            }
        }
    }

    private suspend fun page(beforeId: Long): List<LogRecord> {
        if (query.levels.isEmpty()) return emptyList()
        val logDao = dao() ?: return emptyList()
        return logDao.page(
            beforeId = beforeId,
            priorities = query.levels.map { it.toPriority().name },
            query = query.text,
            filterTags = if (query.tags.isEmpty()) 0 else 1,
            tags = query.tags.toList().ifEmpty { listOf("") },
            filterExcluded = if (query.excludedTags.isEmpty()) 0 else 1,
            excludedTags = query.excludedTags.toList().ifEmpty { listOf("") },
            limit = PAGE_SIZE,
        ).map { it.toRecord() }
    }
}

private fun LogLevel.toPriority(): LogPriority = when (this) {
    LogLevel.TRACE -> LogPriority.VERBOSE
    LogLevel.DEBUG -> LogPriority.DEBUG
    LogLevel.INFO -> LogPriority.INFO
    LogLevel.WARN -> LogPriority.WARN
    LogLevel.ERROR -> LogPriority.ERROR
    LogLevel.FATAL -> LogPriority.ASSERT
}

private fun String.toLevel(): LogLevel = when (this) {
    LogPriority.VERBOSE.name -> LogLevel.TRACE
    LogPriority.DEBUG.name -> LogLevel.DEBUG
    LogPriority.INFO.name -> LogLevel.INFO
    LogPriority.WARN.name -> LogLevel.WARN
    LogPriority.ERROR.name -> LogLevel.ERROR
    LogPriority.ASSERT.name -> LogLevel.FATAL
    else -> LogLevel.INFO
}

private fun DevLogEntity.toRecord(): LogRecord = LogRecord(
    id = LogId(id.toString()),
    time = Instant.fromEpochMilliseconds(timestamp),
    level = priority.toLevel(),
    tag = tag,
    message = message,
    meta = LogMeta(session = session),
)
