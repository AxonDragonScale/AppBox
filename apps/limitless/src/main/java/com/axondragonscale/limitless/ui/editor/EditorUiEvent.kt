package com.axondragonscale.limitless.ui.editor

import com.axondragonscale.limitless.database.entity.HabitEntity

/**
 * Created by Ronak Harkhani on 03/11/24
 */
sealed interface EditorUiEvent {
    data object NavigateBack : EditorUiEvent
    data class UpdateHabit(val habit: HabitEntity) : EditorUiEvent
    data class SaveHabit(val habit: HabitEntity) : EditorUiEvent
}
