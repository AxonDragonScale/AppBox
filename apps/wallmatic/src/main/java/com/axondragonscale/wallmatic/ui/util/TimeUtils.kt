package com.axondragonscale.wallmatic.ui.util

import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.time.Instant

/**
 * Created by Ronak Harkhani on 26/09/24
 */

/** Formats an epoch millis in the design's caption format: `10 Nov · 11:30 PM`. */
fun Long.toShortDateTimeString(): String =
    DateTimeFormatter.ofPattern("dd MMM · hh:mm a", Locale.US)
        .format(
            LocalDateTime.ofInstant(
                java.time.Instant.ofEpochMilli(this),
                ZoneId.systemDefault()
            )
        )

fun Instant.toShortDateTimeString(): String = toEpochMilliseconds().toShortDateTimeString()
