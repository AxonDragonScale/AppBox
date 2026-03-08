package com.axondragonscale.limitless.ui.editor

import com.axondragonscale.limitless.database.entity.GoalEntity
import com.axondragonscale.limitless.database.entity.HabitEntity
import com.axondragonscale.limitless.database.entity.ReminderEntity

/**
 * Created by Ronak Harkhani on 03/11/24
 */
sealed interface EditorUiEvent {
    data object NavigateBack : EditorUiEvent
    data class UpdateHabit(val habit: HabitEntity) : EditorUiEvent
    data class SaveHabit(val habit: HabitEntity) : EditorUiEvent

    // Reminder events
    data object AddReminder : EditorUiEvent
    data class UpdateReminder(val reminder: ReminderEntity) : EditorUiEvent
    data class DeleteReminder(val reminderId: Long) : EditorUiEvent

    // Goal events
    data object AddGoal : EditorUiEvent
    data class UpdateGoal(val goal: GoalEntity) : EditorUiEvent
    data class DeleteGoal(val goalId: Long) : EditorUiEvent
}
