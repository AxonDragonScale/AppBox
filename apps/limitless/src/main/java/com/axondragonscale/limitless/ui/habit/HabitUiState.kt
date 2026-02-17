package com.axondragonscale.limitless.ui.habit

import com.axondragonscale.limitless.database.entity.HabitEntity

/**
 * Created by Ronak Harkhani on 17/02/26
 */
data class HabitUiState(
    val isLoading: Boolean = true,
    val habit: HabitEntity = HabitEntity.new(),
    val todayCount: Int = 0,
)
