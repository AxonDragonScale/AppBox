package com.axondragonscale.limitless.ui.habit.history

import com.axondragonscale.limitless.database.entity.RecordEntity

/**
 * Created by Ronak Harkhani on 29/06/25
 */
sealed interface HistoryUiEvent {
    data object LoadMore : HistoryUiEvent
    data class SaveRecord(val record: RecordEntity) : HistoryUiEvent
    data class DeleteRecord(val id: Long) : HistoryUiEvent
}
