package com.axondragonscale.limitless.ui.habit.calendar

import kotlinx.datetime.LocalDate

/**
 * Created by Ronak Harkhani on 29/06/25
 */
data class HabitCalendarUiState(
    val year: Int = 0,
    val month: Int = 0,  // 1–12
    val habitName: String = "",
    val dayCounts: Map<LocalDate, Int> = emptyMap(),
    val isLoading: Boolean = true,
)
