package com.axondragonscale.commons.devtools.logs.data

import com.axondragonscale.commons.LogPriority
import com.axondragonscale.logviewer.model.LogId
import com.axondragonscale.logviewer.model.LogLevel
import com.axondragonscale.logviewer.model.LogMeta
import com.axondragonscale.logviewer.model.LogRecord
import com.axondragonscale.logviewer.source.LogCapabilities
import com.axondragonscale.logviewer.source.LogFacet
import com.axondragonscale.logviewer.source.LogFacets
import com.axondragonscale.logviewer.source.LogQuery
import com.axondragonscale.logviewer.source.LogSource
import com.axondragonscale.logviewer.source.LogStream
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
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
 * One authority for the level vocabulary: SQLite stores [LogPriority], the viewer speaks
 * [LogLevel], and both directions read this table.
 */
private val Priorities = listOf(
    LogPriority.VERBOSE to LogLevel.TRACE,
    LogPriority.DEBUG to LogLevel.DEBUG,
    LogPriority.INFO to LogLevel.INFO,
    LogPriority.WARN to LogLevel.WARN,
    LogPriority.ERROR to LogLevel.ERROR,
    LogPriority.ASSERT to LogLevel.FATAL,
)

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

    override suspend fun facets(): LogFacets =
        LogFacets(tags = runCatching { dao()?.tags().orEmpty() }.getOrDefault(emptyList()))

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

    private val blocked = MutableStateFlow<String?>(null)
    override val notice: Flow<String?> = blocked.asStateFlow()

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
        val rows = read(beforeId = before)
        if (rows != null) {
            if (rows.isNotEmpty()) {
                val known = loaded.value.mapTo(mutableSetOf()) { it.id }
                loaded.value = loaded.value + rows.filter { it.id !in known }
            }
            older.value = rows.size >= PAGE_SIZE
        }
        loading = false
    }

    override suspend fun clear() {
        if (runCatching { dao()?.clear() }.isFailure) {
            blocked.value = "The logs could not be cleared."
            return
        }
        loaded.value = emptyList()
        older.value = false
        loadFirst()
    }

    override fun close() {
        scope.cancel()
    }

    private suspend fun loadFirst() {
        val rows = read(beforeId = Long.MAX_VALUE) ?: return
        applyFirstPage(rows)
    }

    /** A first successful read establishes the list and its paging state, whenever it arrives. */
    private fun applyFirstPage(rows: List<LogRecord>) {
        loaded.value = rows
        older.value = rows.size >= PAGE_SIZE
        ready.value = true
    }

    /** The newest page plus whatever history is already loaded, newest first. */
    private suspend fun poll() {
        while (scope.isActive) {
            delay(POLL_INTERVAL_MS)
            val rows = read(beforeId = Long.MAX_VALUE) ?: continue
            if (!ready.value) {
                applyFirstPage(rows)
                continue
            }
            if (rows.isEmpty()) continue
            val merged = (rows + loaded.value).distinctBy { it.id }
            if (merged != loaded.value) loaded.value = merged
        }
    }

    /**
     * A page, or null when the database could not answer. A failure goes to [notice] instead of
     * passing for an empty page, so a locked or half-migrated database is visible in the viewer.
     */
    private suspend fun read(beforeId: Long): List<LogRecord>? {
        if (query.levels.isEmpty()) return emptyList()
        val logDao = dao()
        if (logDao == null) {
            blocked.value = "The log store is not open."
            return null
        }
        val tags = SqlAnyOf(query.included(LogFacet.TAG))
        val excludedTags = SqlAnyOf(query.excluded(LogFacet.TAG))
        return runCatching {
            logDao.page(
                beforeId = beforeId,
                priorities = query.levels.map { it.toPriority().name },
                query = query.text,
                includeTags = tags.active,
                tags = tags.values,
                excludeTags = excludedTags.active,
                excludedTags = excludedTags.values,
                limit = PAGE_SIZE,
            ).map { it.toRecord() }
        }
            .onSuccess { blocked.value = null }
            .onFailure { blocked.value = "The log store could not be read." }
            .getOrNull()
    }
}

/**
 * SQLite has no valid empty `IN ()`, so an inactive filter still binds one throwaway value. One
 * place builds the flag and the list together, so they cannot disagree.
 */
private class SqlAnyOf(selected: Set<String>) {
    val active: Boolean = selected.isNotEmpty()
    val values: List<String> = selected.toList().ifEmpty { listOf("") }
}

private fun LogLevel.toPriority(): LogPriority = Priorities.first { it.second == this }.first

private fun String.toLevel(): LogLevel =
    Priorities.firstOrNull { it.first.name == this }?.second ?: LogLevel.INFO

private fun DevLogEntity.toRecord(): LogRecord = LogRecord(
    id = LogId(id.toString()),
    time = Instant.fromEpochMilliseconds(timestamp),
    level = priority.toLevel(),
    tag = tag,
    message = message,
    meta = LogMeta(session = session),
)
