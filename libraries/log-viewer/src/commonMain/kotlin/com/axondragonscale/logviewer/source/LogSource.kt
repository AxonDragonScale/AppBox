package com.axondragonscale.logviewer.source

import com.axondragonscale.logviewer.model.LogRecord
import kotlinx.coroutines.flow.StateFlow

/** Created by Ronak Harkhani on 11/22/26 */

/**
 * Where log records come from. It answers the questions the viewer asks instead of handing it a
 * format to parse: a query in, pages and a live list out.
 *
 * The source promises, for the life of a [LogStream]: records stay strictly newest → oldest, ids are
 * stable and unique, and each change is one emission. The viewer never sorts or merges.
 */
interface LogSource {

    val capabilities: LogCapabilities

    /** Tags this source can filter by; empty when it cannot enumerate them. */
    suspend fun tags(): List<String> = emptyList()

    fun open(query: LogQuery): LogStream
}

/**
 * One open view of a source. The viewer creates it when the query changes and closes it when the
 * query changes again or the screen goes away; closing releases whatever the source started.
 */
interface LogStream : AutoCloseable {

    /** Newest first. Prepends carry live records; the tail grows with older pages. */
    val entries: StateFlow<List<LogRecord>>

    /** False until the first page has been read, so the screen can say "reading", not "empty". */
    val isLoaded: StateFlow<Boolean>

    val hasOlder: StateFlow<Boolean>

    /** A one-line explanation when the source cannot deliver, e.g. denied log access. */
    val notice: StateFlow<String?>? get() = null

    /** Appends the page older than the current tail. A no-op when [hasOlder] is false. */
    suspend fun loadOlder()

    /** Deletes at the source. Only called when `capabilities.clear`. */
    suspend fun clear()
}
