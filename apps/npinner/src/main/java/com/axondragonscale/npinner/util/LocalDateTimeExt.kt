package com.axondragonscale.npinner.util

import android.text.format.DateUtils
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format
import kotlinx.datetime.format.Padding
import kotlinx.datetime.format.char
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Instant

/**
 * Created by Ronak Harkhani on 01/05/23
 */

private val dateFormat = LocalDate.Format {
    monthName(kotlinx.datetime.format.MonthNames.ENGLISH_ABBREVIATED)
    char(' ')
    day()
}

val LocalDate.formatted: String
    get() = this.format(dateFormat).uppercase()

fun Long.toLocalDate(): LocalDate = Instant.fromEpochMilliseconds(this)
    .toLocalDateTime(TimeZone.currentSystemDefault())
    .date

private val timeFormat = LocalTime.Format {
    amPmHour(Padding.NONE)
    char(':')
    minute()
    char(' ')
    amPmMarker("AM", "PM")
}

val LocalTime.formatted: String
    get() = this.format(timeFormat).uppercase()

val Long.relativeTimeSpan: String
    get() = DateUtils.getRelativeTimeSpanString(
        this,
        System.currentTimeMillis(),
        DateUtils.SECOND_IN_MILLIS,
        DateUtils.FORMAT_ABBREV_RELATIVE,
    ).toString()

fun LocalDateTime.toEpochMillis(): Long =
    this.toInstant(TimeZone.UTC).toEpochMilliseconds()
