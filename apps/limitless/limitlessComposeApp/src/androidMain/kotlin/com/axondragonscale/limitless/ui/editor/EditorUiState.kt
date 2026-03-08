package com.axondragonscale.limitless.ui.editor

import com.axondragonscale.limitless.database.entity.GoalEntity
import com.axondragonscale.limitless.database.entity.HabitEntity
import com.axondragonscale.limitless.database.entity.ReminderEntity

/**
 * Created by Ronak Harkhani on 03/11/24
 */
data class EditorUiState(
    val isLoading: Boolean = true,
    val habit: HabitEntity = HabitEntity.new(),
    val reminders: List<ReminderEntity> = emptyList(),
    val goals: List<GoalEntity> = emptyList(),
)
