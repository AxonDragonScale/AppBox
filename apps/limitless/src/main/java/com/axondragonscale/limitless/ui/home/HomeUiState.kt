package com.axondragonscale.limitless.ui.home

import com.axondragonscale.limitless.model.HabitHeatMapData

/**
 * Created by Ronak Harkhani on 01/11/24
 */
data class HomeUiState(
    val isLoading: Boolean = true,
    val habits: List<HabitHeatMapData> = emptyList(),
    val highlightCurrentDay: Boolean = true,
)
