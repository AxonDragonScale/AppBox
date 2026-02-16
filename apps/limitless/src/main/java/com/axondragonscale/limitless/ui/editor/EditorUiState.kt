package com.axondragonscale.limitless.ui.editor

import com.axondragonscale.limitless.database.entity.HabitEntity

/**
 * Created by Ronak Harkhani on 03/11/24
 */
data class EditorUiState(
    val isLoading: Boolean = true,
    val habit: HabitEntity = HabitEntity.new(),
)
