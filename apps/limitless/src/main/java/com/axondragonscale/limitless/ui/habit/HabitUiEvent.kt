package com.axondragonscale.limitless.ui.habit

/**
 * Created by Ronak Harkhani on 17/02/26
 */
sealed interface HabitUiEvent {
    data object NavigateBack : HabitUiEvent
    data object NavigateToEditor : HabitUiEvent
    data class RecordHabit(val message: String) : HabitUiEvent
}
