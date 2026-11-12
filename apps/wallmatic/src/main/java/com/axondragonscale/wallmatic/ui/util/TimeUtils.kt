package com.axondragonscale.wallmatic.ui.util

import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Created by Ronak Harkhani on 26/09/24
 */

/** Formats an epoch millis in the design's caption format: `10 Nov · 11:30 PM`. */
fun Long.toShortDateTimeString(): String =
    DateTimeFormatter.ofPattern("dd MMM · hh:mm a", Locale.US)
        .format(
            LocalDateTime.ofInstant(
                Instant.ofEpochMilli(this),
                ZoneId.systemDefault()
            )
        )

/** Formats an epoch millis for logs: `yyyy-MM-dd'T'HH:mm:ss`. */
fun Long.toTimestampString(): String =
    DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")
        .format(
            LocalDateTime.ofInstant(
                Instant.ofEpochMilli(this),
                ZoneId.systemDefault()
            )
        )
