package com.axondragonscale.logviewer.model

import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format
import kotlinx.datetime.format.char
import kotlinx.datetime.toLocalDateTime
import kotlin.jvm.JvmInline
import kotlin.time.Instant

/** Created by Ronak Harkhani on 11/22/26 */

/** An opaque, stable identity for one record within its source. */
@JvmInline
value class LogId(val value: String)

/**
 * Fields a source may have, may lack, or spell differently. `null` means the source does not have
 * the field; the viewer hides it rather than inventing a value.
 */
data class LogMeta(
    val pid: Int? = null,
    val tid: Int? = null,
    val packageName: String? = null,
    val session: String? = null,
)

/** One log record, newest first when listed. [message] is complete; the row trims it. */
data class LogRecord(
    val id: LogId,
    val time: Instant,
    val level: LogLevel,
    val tag: String,
    val message: String,
    val meta: LogMeta = LogMeta(),
) {
    /** The row's one line; a trace stays available in the detail. */
    val firstLine: String get() = message.substringBefore('\n')

    /** Everything below [firstLine] — one exception, one block — split once for every reader. */
    val traceLines: List<String> get() = message.lineSequence().drop(1).toList()
}

/** "2026-11-22 14:03:51.208" — the one time format the viewer prints, in the device's zone. */
internal fun Instant.logTime(): String =
    toLocalDateTime(TimeZone.currentSystemDefault()).format(LogTime)

/** The line a share copies: full time, level letter, tag and the whole message. */
internal fun LogRecord.toLogLine(): String = "${time.logTime()} ${level.letter} $tag: $message"

private val LogTime = LocalDateTime.Format {
    year()
    char('-')
    monthNumber()
    char('-')
    day()
    char(' ')
    hour()
    char(':')
    minute()
    char(':')
    second()
    char('.')
    secondFraction(3)
}
