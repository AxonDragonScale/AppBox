package com.axondragonscale.qrqanvas.ui.scanner

/**
 * Created by Ronak Harkhani on 09/07/25
 */
sealed interface ScannerUiEffect {
    data class OpenUrl(val url: String) : ScannerUiEffect
    data class CopyToClipboard(val text: String) : ScannerUiEffect
    data class ShareText(val text: String) : ScannerUiEffect
    data class ShowSnackbar(val message: String) : ScannerUiEffect
}
