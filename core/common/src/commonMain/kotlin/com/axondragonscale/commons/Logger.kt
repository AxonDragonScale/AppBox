package com.axondragonscale.commons

import kotlin.concurrent.Volatile

/**
 * Created by Ronak Harkhani on 23 January 2026.
 */

object Logger {
    /**
     * Use this to disable logging globally.
     */
    var isLoggingEnabled = true

    /**
     * Replaced as a whole on [addSink], so a log call iterates without locking. Registration happens
     * once at startup, which makes the lost update of two concurrent adds a non-issue.
     */
    @Volatile
    private var extraSinks: Array<LogSink> = emptyArray()

    /** Sends every record to [sink] as well as to the platform logger. Safe to call once, early. */
    fun addSink(sink: LogSink) {
        extraSinks += sink
    }

    fun removeSink(sink: LogSink) {
        extraSinks = extraSinks.filterNot { it === sink }.toTypedArray()
    }

    internal fun dispatchToSinks(priority: LogPriority, tag: String, message: String) {
        for (sink in extraSinks) {
            runCatching { sink.log(priority, tag, message) }
        }
    }
}

/** A destination for log records besides the platform logger, such as a database. */
fun interface LogSink {
    fun log(priority: LogPriority, tag: String, message: String)
}

fun Any.logD(msg: MsgProvider) = this.log(LogPriority.DEBUG, msg)
fun Any.logE(msg: MsgProvider) = this.log(LogPriority.ERROR, msg)
fun Any.logI(msg: MsgProvider) = this.log(LogPriority.INFO, msg)
fun Any.logW(msg: MsgProvider) = this.log(LogPriority.WARN, msg)
fun Any.logV(msg: MsgProvider) = this.log(LogPriority.VERBOSE, msg)
fun Any.logA(msg: MsgProvider) = this.log(LogPriority.ASSERT, msg)

fun Any.log(e: ThrowableProvider) = this.log(LogPriority.ERROR) {
    val throwable = e()
    "Handled ${throwable::class.simpleName}:\n" + throwable.stackTraceToString()
}

// -------------------- Private Helper Functions --------------------

private fun Any.log(priority: LogPriority, msg: MsgProvider) {
    if (!Logger.isLoggingEnabled) return
    val tag = this.getTag()
    val message = msg()
    dispatchLog(priority, tag, message)
    Logger.dispatchToSinks(priority, tag, message)
}

internal fun Any.getTag() = when {
    this is String -> this
    else -> this::class.simpleName ?: "Unknown"
}

typealias MsgProvider = () -> String
typealias ThrowableProvider = () -> Throwable

enum class LogPriority { VERBOSE, DEBUG, INFO, WARN, ERROR, ASSERT }

/**
 * Platform-specific log dispatch. Each target provides an actual implementation.
 */
internal expect fun dispatchLog(priority: LogPriority, tag: String, message: String)
