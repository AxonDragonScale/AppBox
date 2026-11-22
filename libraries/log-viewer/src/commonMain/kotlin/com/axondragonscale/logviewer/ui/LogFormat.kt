package com.axondragonscale.logviewer.ui

import com.axondragonscale.logviewer.model.LogRecord
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Instant

/** Created by Ronak Harkhani on 11/22/26 */

/** "11-22 14:03:51.208" — the row's date and time, in the device's zone. */
internal fun Instant.logRowTime(): String {
    val dateTime = toLocalDateTime(TimeZone.currentSystemDefault())
    val date = dateTime.date
    return buildString {
        append(date.month.number.toString().padStart(2, '0'))
        append('-')
        append(date.day.toString().padStart(2, '0'))
        append(' ')
        append(dateTime.time.withMillis())
    }
}

/** "2026-11-22 14:03:51.208" — the detail's unabbreviated form. */
internal fun Instant.logFullTime(): String {
    val dateTime = toLocalDateTime(TimeZone.currentSystemDefault())
    val date = dateTime.date
    return buildString {
        append(date.year)
        append('-')
        append(date.month.number.toString().padStart(2, '0'))
        append('-')
        append(date.day.toString().padStart(2, '0'))
        append(' ')
        append(dateTime.time.withMillis())
    }
}

private fun LocalTime.withMillis(): String = buildString {
    append(hour.toString().padStart(2, '0'))
    append(':')
    append(minute.toString().padStart(2, '0'))
    append(':')
    append(second.toString().padStart(2, '0'))
    append('.')
    append((nanosecond / 1_000_000).toString().padStart(3, '0'))
}

internal fun LogRecord.toLogLine(): String = "${time.logFullTime()} ${level.letter} $tag: $message"
