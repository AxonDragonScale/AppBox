package com.axondragonscale.logviewer.source

import com.axondragonscale.logviewer.model.LogRecord
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Created by Ronak Harkhani on 11/22/26 */

/**
 * A source over a fixed list, for previews and hosts that already hold their records. It filters in
 * memory, pages from the list, and has nothing to clear.
 *
 * Capabilities default to what the records themselves carry, so a source built from records with
 * package names does not also have to declare them.
 */
class InMemoryLogSource(
    records: List<LogRecord>,
    override val capabilities: LogCapabilities = LogCapabilities(
        history = true,
        pid = records.any { it.meta.pid != null },
        tid = records.any { it.meta.tid != null },
        packageName = records.any { it.meta.packageName != null },
        sessions = records.any { it.meta.session != null },
    ),
    /** Give each of two such sources a distinct key, or their viewers share one ViewModel. */
    override val key: String = "in-memory",
) : LogSource {

    private val all = records.sortedByDescending { it.time }

    override suspend fun facets(): LogFacets = LogFacets().withRecords(all)

    override fun open(query: LogQuery): LogStream = Stream(all.filter(query::matches))

    private class Stream(private val filtered: List<LogRecord>) : LogStream {

        private val loaded = MutableStateFlow(emptyList<LogRecord>())
        private val ready = MutableStateFlow(false)
        private val older = MutableStateFlow(false)

        override val entries: StateFlow<List<LogRecord>> = loaded.asStateFlow()
        override val isLoaded: StateFlow<Boolean> = ready.asStateFlow()
        override val hasOlder: StateFlow<Boolean> = older.asStateFlow()

        init {
            loaded.value = filtered.take(PAGE)
            older.value = filtered.size > PAGE
            ready.value = true
        }

        override suspend fun loadOlder() {
            if (!older.value) return
            val next = filtered.drop(loaded.value.size).take(PAGE)
            loaded.value = loaded.value + next
            older.value = loaded.value.size < filtered.size
        }

        override suspend fun clear() = Unit

        override fun close() = Unit
    }

    private companion object {
        const val PAGE = 200
    }
}
