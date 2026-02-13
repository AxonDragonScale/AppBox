package com.axondragonscale.qrqanvas.ui.history

/**
 * Created by Ronak Harkhani on 09/07/25
 */
sealed interface HistoryUiEvent {
    data class ItemClicked(val id: Long) : HistoryUiEvent
    data class DeleteItem(val id: Long) : HistoryUiEvent
    data object ClearAll : HistoryUiEvent
    data object ConfirmClearAll : HistoryUiEvent
    data object DismissClearAll : HistoryUiEvent
    data class CopyItem(val rawValue: String) : HistoryUiEvent
    data class ShareItem(val rawValue: String) : HistoryUiEvent
    data class OpenItem(val rawValue: String) : HistoryUiEvent
}
