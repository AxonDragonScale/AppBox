package com.axondragonscale.limitless.ui.habit.history

/**
 * Created by Ronak Harkhani on 29/06/25
 */
sealed interface HistoryUiEvent {
    data object LoadMore : HistoryUiEvent
}
