package com.axondragonscale.limitless.ui.stats.model

/**
 * Created by Ronak Harkhani on 21/07/25
 *
 * Data for the Time of Day histogram widget.
 * Shows record distribution across hours of the day.
 *
 * @param hourCounts Record counts per hour. Size = 24, index = hour (0 = midnight, 23 = 11 PM).
 * @param peakHour The hour with the highest count, or -1 if no records exist.
 */
data class TimeOfDayData(
    val hourCounts: List<Int>,
    val peakHour: Int,
)
