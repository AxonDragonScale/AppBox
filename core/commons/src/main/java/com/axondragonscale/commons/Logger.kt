package com.axondragonscale.commons

import android.util.Log

/**
 * Created by Ronak Harkhani on 23 January 2026.
 */

object Logger {
    /**
     * Use this to disable logging globally.
     */
    var isLoggingEnabled = true

    /**
     * Use this to force logging in release builds. False by default.
     */
    var usePrintlnLogs = false
}

/**
 * Log methods to be used on class/object instances or on TAG strings.
 * ```
 * this.logE { "Something went wrong" } // where this is the enclosing class or object (not String)
 * TAG.logE { "Something went wrong" }  // where const val TAG = "MyTag"
 * ```
 * Using `this.logX` may not work properly inside lambdas. Prefer `TAG.logX` inside lambdas.
 */

fun Any.logD(msg: MsgProvider) = this.log(Log.DEBUG, msg)
fun Any.logE(msg: MsgProvider) = this.log(Log.ERROR, msg)
fun Any.logI(msg: MsgProvider) = this.log(Log.INFO, msg)
fun Any.logW(msg: MsgProvider) = this.log(Log.WARN, msg)
fun Any.logV(msg: MsgProvider) = this.log(Log.VERBOSE, msg)
fun Any.logA(msg: MsgProvider) = this.log(Log.ASSERT, msg)

fun Any.log(e: ExceptionProvider) = this.log(Log.ERROR) { "Handled Exception:\n" + e().stackTraceToString() }

// -------------------- Private Helper Functions --------------------

private fun Any.log(priority: Int, msg: MsgProvider) {
    if (!Logger.isLoggingEnabled) return
    val message = msg()
    if (Logger.usePrintlnLogs) println("#${this.getTag()}: $message")
    else Log.println(priority, "#${this.getTag()}", message)
}

private fun Any.getTag() = when {
    this is String -> this
    this.javaClass.simpleName == "Companion" -> this.javaClass.enclosingClass.simpleName
    else -> this.javaClass.simpleName
}

private typealias MsgProvider = () -> String
private typealias ExceptionProvider = () -> Exception
