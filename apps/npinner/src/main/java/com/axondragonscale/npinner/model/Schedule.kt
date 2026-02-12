package com.axondragonscale.npinner.model

import androidx.annotation.Keep
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

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
        fun newInstance() = Schedule(
            date = LocalDate.now(),
            time = LocalTime.now().plusMinutes(10),
            type = ScheduleType.DAY,
        )

    }

    val asLocalDateTime: LocalDateTime by lazy { date.atTime(time) }

    val isFuture by lazy { type != null || asLocalDateTime.isAfter(LocalDateTime.now()) }

}
