package com.axondragonscale.logviewer.source

import com.axondragonscale.logviewer.model.LogRecord
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Created by Ronak Harkhani on 11/22/26 */

/**
 * A source over a fixed list, for previews and hosts that already hold their records. It filters in
 * memory, pages from the list, and has nothing to clear.
 */
class InMemoryLogSource(
    records: List<LogRecord>,
    override val capabilities: LogCapabilities = LogCapabilities(),
) : LogSource {

    private val all = records.sortedByDescending { it.id.value }

    override fun open(query: LogQuery): LogStream = Stream(all.filter { it.matches(query) })

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

internal fun LogRecord.matches(query: LogQuery): Boolean {
    val packageName = meta.packageName
    return level in query.levels &&
        tag !in query.excludedTags &&
        packageName !in query.excludedPackages &&
        (query.text.isBlank() || tag.contains(query.text, ignoreCase = true) ||
            message.contains(query.text, ignoreCase = true)) &&
        (query.tags.isEmpty() || tag in query.tags) &&
        (query.packages.isEmpty() || packageName in query.packages)
}
