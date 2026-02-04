package com.axondragonscale.wallmatic.ui.util

import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Created by Ronak Harkhani on 26/09/24
 */

/**
 * Converts this epoch millis Long to string in the format: `dd MMM - HH:mm a`
 *
 * Locale.US is to capitalize the am/pm
 */
fun Long.toDateTimeString(): String =
    DateTimeFormatter.ofPattern("dd MMM - hh:mm a", Locale.US)
        .format(
            LocalDateTime.ofInstant(
                Instant.ofEpochMilli(this),
                ZoneId.systemDefault()
            )
        )

/**
 * Converts this epoch millis Long to string in the format: `yyyy-MM-dd'T'HH:mm:ss`
 */
fun Long.toTimestampString(): String =
    DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")
        .format(
            LocalDateTime.ofInstant(
                Instant.ofEpochMilli(this),
                ZoneId.systemDefault()
            )
        )
