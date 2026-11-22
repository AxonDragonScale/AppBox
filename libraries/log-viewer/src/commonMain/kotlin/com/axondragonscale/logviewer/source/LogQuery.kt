package com.axondragonscale.logviewer.source

import com.axondragonscale.logviewer.model.LogLevel

/** Created by Ronak Harkhani on 11/22/26 */

/** What the viewer is asking for. The source applies it however it can: SQL, or in memory. */
data class LogQuery(
    val levels: Set<LogLevel> = LogLevel.entries.toSet(),
    val text: String = "",
    val tags: Set<String> = emptySet(),
    val excludedTags: Set<String> = emptySet(),
    val packages: Set<String> = emptySet(),
    val excludedPackages: Set<String> = emptySet(),
)

/**
 * What a source can do. The viewer renders only what is true here: no "load older" without
 * [history], no clear action without [clear], no meta fields a source does not have.
 */
data class LogCapabilities(
    val live: Boolean = false,
    val history: Boolean = false,
    val pid: Boolean = false,
    val tid: Boolean = false,
    val packageName: Boolean = false,
    val sessions: Boolean = false,
    val clear: Boolean = false,
)
