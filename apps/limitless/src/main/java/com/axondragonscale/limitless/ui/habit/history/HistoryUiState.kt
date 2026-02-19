package com.axondragonscale.limitless.ui.habit.history

import com.axondragonscale.limitless.database.entity.RecordEntity

/**
 * Created by Ronak Harkhani on 29/06/25
 */
data class HistoryUiState(
    val records: List<RecordEntity> = emptyList(),
    val isLoading: Boolean = true,
    val isLoadingMore: Boolean = false,
    val hasMore: Boolean = false,
)
