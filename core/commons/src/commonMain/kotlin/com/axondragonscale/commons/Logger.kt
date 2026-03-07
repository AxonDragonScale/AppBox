package com.axondragonscale.commons

/**
 * Created by Ronak Harkhani on 23 January 2026.
 */

object Logger {
    /**
     * Use this to disable logging globally.
     */
    var isLoggingEnabled = true

    /**
     * Use this to force println-based logs instead of platform log APIs.
     */
    var usePrintlnLogs = false
}

fun Any.logD(msg: MsgProvider) = this.log(LogPriority.DEBUG, msg)
fun Any.logE(msg: MsgProvider) = this.log(LogPriority.ERROR, msg)
fun Any.logI(msg: MsgProvider) = this.log(LogPriority.INFO, msg)
fun Any.logW(msg: MsgProvider) = this.log(LogPriority.WARN, msg)
fun Any.logV(msg: MsgProvider) = this.log(LogPriority.VERBOSE, msg)
fun Any.logA(msg: MsgProvider) = this.log(LogPriority.ASSERT, msg)

fun Any.log(e: ExceptionProvider) = this.log(LogPriority.ERROR) { "Handled Exception:\n" + e().stackTraceToString() }

// -------------------- Private Helper Functions --------------------

private fun Any.log(priority: LogPriority, msg: MsgProvider) {
    if (!Logger.isLoggingEnabled) return
    val message = msg()
    if (Logger.usePrintlnLogs) println("#${this.getTag()}: $message")
    else dispatchLog(priority, this.getTag(), message)
}

internal fun Any.getTag() = when {
    this is String -> this
    else -> this::class.simpleName ?: "Unknown"
}

typealias MsgProvider = () -> String
typealias ExceptionProvider = () -> Exception

enum class LogPriority { VERBOSE, DEBUG, INFO, WARN, ERROR, ASSERT }

/**
 * Platform-specific log dispatch. Each target provides an actual implementation.
 */
internal expect fun dispatchLog(priority: LogPriority, tag: String, message: String)
