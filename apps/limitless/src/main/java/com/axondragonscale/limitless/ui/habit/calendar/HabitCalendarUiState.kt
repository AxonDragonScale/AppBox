package com.axondragonscale.limitless.ui.habit.calendar

import com.axondragonscale.limitless.database.entity.HabitEntity
import kotlinx.datetime.LocalDate

/**
 * Created by Ronak Harkhani on 29/06/25
 */
data class HabitCalendarUiState(
    val isLoading: Boolean = true,
    val habit: HabitEntity = HabitEntity.new(),
    val year: Int = 0,
    val month: Int = 0,  // 1–12
    val dayCounts: Map<LocalDate, Int> = emptyMap(),
)
