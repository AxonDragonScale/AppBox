package com.axondragonscale.commons

import android.util.Log

/**
 * Created by Ronak Harkhani on 23 January 2026.
 */

internal actual fun dispatchLog(priority: LogPriority, tag: String, message: String) {
    val androidPriority = when (priority) {
        LogPriority.VERBOSE -> Log.VERBOSE
        LogPriority.DEBUG   -> Log.DEBUG
        LogPriority.INFO    -> Log.INFO
        LogPriority.WARN    -> Log.WARN
        LogPriority.ERROR   -> Log.ERROR
        LogPriority.ASSERT  -> Log.ASSERT
    }
    Log.println(androidPriority, "#$tag", message)
}
