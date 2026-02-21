package com.axondragonscale.commons

import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.Month
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Instant

/**
 * Created by Ronak Harkhani on 30/06/25
 *
 * DateTime formatting extensions shared across all apps.
 */

// region Month Extensions

/** Short 3-letter month abbreviation: "Jan", "Feb", … */
val Month.abbreviated: String
    get() = name.lowercase().replaceFirstChar { it.uppercase() }.take(3)

/** Full capitalized month name: "January", "February", … */
val Month.displayName: String
    get() = name.lowercase().replaceFirstChar { it.uppercase() }

// endregion

// region LocalDate Extensions

/**
 * Formats a [LocalDate] as "Jan 5, 2025".
 */
fun LocalDate.formatShort(): String =
    "${month.abbreviated} $day, $year"

/**
 * Returns the number of days in the given [month] of [year], correctly handling leap years.
 * Delegates entirely to [kotlinx.datetime] — no manual leap-year arithmetic needed.
 */
fun daysInMonth(year: Int, month: Int): Int =
    LocalDate(year, month, 1)
        .plus(1, DateTimeUnit.MONTH)
        .minus(1, DateTimeUnit.DAY)
        .day

/**
 * Returns the number of days between [start] (inclusive) and [end] (exclusive).
 * A negative result means [end] is before [start].
 */
fun daysBetween(start: LocalDate, end: LocalDate): Int =
    (end.toEpochDays() - start.toEpochDays()).toInt()

// endregion

// region LocalTime Extensions

/**
 * Formats a [LocalTime] as "9:30 AM" or "12:05 PM".
 */
fun LocalTime.formatAmPm(): String {
    val h = if (hour % 12 == 0) 12 else hour % 12
    val m = minute.toString().padStart(2, '0')
    val amPm = if (hour < 12) "AM" else "PM"
    return "$h:$m $amPm"
}

// endregion

// region Instant Extensions

/**
 * Formats an [Instant] as "Jan 5, 2025 at 9:30 AM" in the given [tz].
 */
fun Instant.formatDateTime(tz: TimeZone = TimeZone.currentSystemDefault()): String {
    val dt = toLocalDateTime(tz)
    return "${dt.date.formatShort()} at ${dt.time.formatAmPm()}"
}

/**
 * Formats an [Instant] as "9:30 AM" in the given [tz].
 */
fun Instant.formatTime(tz: TimeZone = TimeZone.currentSystemDefault()): String =
    toLocalDateTime(tz).time.formatAmPm()

// endregion
