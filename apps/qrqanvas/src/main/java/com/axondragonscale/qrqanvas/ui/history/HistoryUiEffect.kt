package com.axondragonscale.qrqanvas.ui.history

/**
 * Created by Ronak Harkhani on 09/07/25
 */
sealed interface HistoryUiEffect {
    data class CopyToClipboard(val text: String) : HistoryUiEffect
    data class ShareText(val text: String) : HistoryUiEffect
    data class OpenUrl(val url: String) : HistoryUiEffect
    data class ShowSnackbar(val message: String) : HistoryUiEffect
}
