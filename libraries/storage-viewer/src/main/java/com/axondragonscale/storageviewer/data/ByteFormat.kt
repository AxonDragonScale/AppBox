package com.axondragonscale.storageviewer.data

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToLong

/** Created by Ronak on 14/11/26 */

private val BYTE_UNITS = listOf("B", "KB", "MB", "GB")
private const val BYTES_PER_UNIT = 1024.0
private const val DECIMAL_LIMIT = 10
private const val TENTHS_PER_UNIT = 1024 * 10
private const val TENTHS_DECIMAL_LIMIT = DECIMAL_LIMIT * 10

private const val MINUTE_MS = 60_000L
private const val HOUR_MS = 60 * MINUTE_MS
private const val DAY_MS = 24 * HOUR_MS
private const val WEEK_MS = 7 * DAY_MS
private const val DATE_PATTERN = "d MMM yyyy"

/**
 * A size as the files surface prints it: 1024-based, one decimal below 10 units, an integer from
 * 10 up, and never a repeated unit. Internal to this module, so every UI package can call it.
 */
internal fun formatBytes(bytes: Long): String {
    if (bytes < BYTES_PER_UNIT) return "$bytes B"
    var value = bytes / BYTES_PER_UNIT
    var unit = 1
    while (value >= BYTES_PER_UNIT && unit < BYTE_UNITS.lastIndex) {
        value /= BYTES_PER_UNIT
        unit++
    }
    var tenths = (value * 10).roundToLong()
    // A value just under the next unit rounds up to 1024, so promote instead of printing it.
    if (tenths >= TENTHS_PER_UNIT && unit < BYTE_UNITS.lastIndex) {
        value /= BYTES_PER_UNIT
        unit++
        tenths = (value * 10).roundToLong()
    }
    val label = BYTE_UNITS[unit]
    return if (tenths < TENTHS_DECIMAL_LIMIT) {
        "${tenths / 10}.${tenths % 10} $label"
    } else {
        "${value.roundToLong()} $label"
    }
}

/**
 * When a file was last written, in the one sentence the files surface uses. [now] is a parameter
 * so the same millis always print the same label.
 */
internal fun formatRelativeTime(millis: Long, now: Long = System.currentTimeMillis()): String {
    if (millis <= 0L) return "Modified unknown"
    val elapsed = (now - millis).coerceAtLeast(0L)
    return when {
        elapsed < MINUTE_MS -> "Modified just now"
        elapsed < HOUR_MS -> elapsedAgo(elapsed / MINUTE_MS, "minute")
        elapsed < DAY_MS -> elapsedAgo(elapsed / HOUR_MS, "hour")
        elapsed < 2 * DAY_MS -> "Modified yesterday"
        elapsed < WEEK_MS -> elapsedAgo(elapsed / DAY_MS, "day")
        else -> "Modified on ${SimpleDateFormat(DATE_PATTERN, Locale.ENGLISH).format(Date(millis))}"
    }
}

private fun elapsedAgo(count: Long, unit: String): String =
    "Modified $count $unit${if (count == 1L) "" else "s"} ago"
