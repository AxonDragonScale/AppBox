package com.axondragonscale.commons

/**
 * Created by Ronak Harkhani on 23 January 2026.
 */

internal actual fun dispatchLog(priority: LogPriority, tag: String, message: String) {
    println("[${priority.name}] #$tag: $message")
}
