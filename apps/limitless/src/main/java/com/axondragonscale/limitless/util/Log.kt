package com.axondragonscale.limitless.util

import android.util.Log

/**
 * Created by Ronak Harkhani on 30/10/24
 */

/**
 * Use this to disable logging globally.
 */
var isLoggingEnabled = true

/**
 * Use this to force logging in release builds. False by default.
 */
var usePrintlnLogs = false

/**
 * Log methods to be used on class/object instances or on TAG strings.
 * ```
 * this.logE("Something went wrong") // where this is the enclosing class or object (not String)
 * TAG.logE("Something went wrong")  // where const val TAG = "MyTag"
 * ```
 * Using `this.logX` may not work properly inside lambdas. Prefer `TAG.logX` inside lambdas.
 */

fun Any.logD(msg: String) = this.log(Log.DEBUG, msg)
fun Any.logE(msg: String) = this.log(Log.ERROR, msg)
fun Any.logI(msg: String) = this.log(Log.INFO, msg)
fun Any.logW(msg: String) = this.log(Log.WARN, msg)
fun Any.logV(msg: String) = this.log(Log.VERBOSE, msg)
fun Any.logA(msg: String) = this.log(Log.ASSERT, msg)

// -------------------- Private Helper Functions --------------------

private fun Any.log(priority: Int, msg: String) {
    if (!isLoggingEnabled) return
    if (usePrintlnLogs) println("#${this.getTag()}: $msg")
    else Log.println(priority, "#${this.getTag()}", msg)
}

private fun Any.getTag() = when {
    this is String -> this
    this.javaClass.simpleName == "Companion" -> this.javaClass.enclosingClass.simpleName
    else -> this.javaClass.simpleName
}

