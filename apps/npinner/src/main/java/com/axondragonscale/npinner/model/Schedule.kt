package com.axondragonscale.npinner.model

import androidx.annotation.Keep
import kotlin.time.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Duration.Companion.minutes

/**
 * Created by Ronak Harkhani on 30/04/23
 */

@Keep
data class Schedule(
    val date: LocalDate,
    val time: LocalTime,
    val type: ScheduleType?,
) {
    companion object {

        /**
         * Creates default Schedule. Scheduled 10 min from now and repeats every day.
         */
        fun newInstance(): Schedule {
            val tz = TimeZone.currentSystemDefault()
            val later = Clock.System.now().plus(10.minutes).toLocalDateTime(tz)
            return Schedule(
                date = later.date,
                time = later.time,
                type = ScheduleType.DAY,
            )
        }

    }

    val asLocalDateTime: LocalDateTime by lazy { LocalDateTime(date, time) }

    val isFuture by lazy {
        type != null || asLocalDateTime > Clock.System.now()
            .toLocalDateTime(TimeZone.currentSystemDefault())
    }

}
