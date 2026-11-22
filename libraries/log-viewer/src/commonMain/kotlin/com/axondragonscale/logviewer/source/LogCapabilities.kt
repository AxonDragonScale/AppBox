package com.axondragonscale.logviewer.source

/** Created by Ronak Harkhani on 11/22/26 */

/**
 * What a source can do. The viewer renders only what is true here: no "load older" without
 * [history], no clear action without [clear], no meta fields a source does not have. [packageName]
 * means records carry one, which is also what a package rule filters on.
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
