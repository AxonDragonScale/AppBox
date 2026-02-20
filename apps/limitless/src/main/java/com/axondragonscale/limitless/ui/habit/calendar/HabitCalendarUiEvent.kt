package com.axondragonscale.limitless.ui.habit.calendar

import kotlinx.datetime.LocalDate

/**
 * Created by Ronak Harkhani on 29/06/25
 */
sealed interface HabitCalendarUiEvent {
    data object PreviousMonth : HabitCalendarUiEvent
    data object NextMonth : HabitCalendarUiEvent
    data class NavigateToDayDetail(val date: LocalDate) : HabitCalendarUiEvent
    data class AddRecord(val message: String, val timestamp: Long) : HabitCalendarUiEvent
}
