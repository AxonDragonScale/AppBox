package com.axondragonscale.limitless.model

import kotlinx.datetime.LocalDate

/**
 * Created by Ronak Harkhani on 09/11/24
 */
data class HabitHeatMapData(
    val habitId: Long,
    val habitName: String,
    val dayCounts: Map<LocalDate, Int>,
    val maxCount: Int,
    val color: HabitColor,
)
