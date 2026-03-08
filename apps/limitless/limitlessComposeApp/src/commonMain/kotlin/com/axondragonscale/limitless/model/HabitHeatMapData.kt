package com.axondragonscale.limitless.model

import com.axondragonscale.limitless.database.entity.HabitEntity
import kotlinx.datetime.LocalDate

/**
 * Created by Ronak Harkhani on 09/11/24
 */
data class HabitHeatMapData(
    val habit: HabitEntity,
    val dayCounts: Map<LocalDate, Int>,
    val maxCount: Int,
)
