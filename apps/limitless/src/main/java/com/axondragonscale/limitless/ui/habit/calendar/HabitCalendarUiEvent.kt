package com.axondragonscale.limitless.ui.habit.calendar

import com.axondragonscale.limitless.database.entity.RecordEntity
import kotlinx.datetime.LocalDate

/**
 * Created by Ronak Harkhani on 29/06/25
 */
sealed interface HabitCalendarUiEvent {
    data object PreviousMonth : HabitCalendarUiEvent
    data object NextMonth : HabitCalendarUiEvent
    data class NavigateToDayDetail(val date: LocalDate) : HabitCalendarUiEvent
    data class SaveRecord(val record: RecordEntity) : HabitCalendarUiEvent
}
