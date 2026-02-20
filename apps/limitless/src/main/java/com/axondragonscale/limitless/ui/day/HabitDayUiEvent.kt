package com.axondragonscale.limitless.ui.day

/**
 * Created by Ronak Harkhani on 29/06/25
 */
sealed interface HabitDayUiEvent {
    data object NavigateBack : HabitDayUiEvent
    data class AddRecord(val message: String, val timestamp: Long) : HabitDayUiEvent
}
