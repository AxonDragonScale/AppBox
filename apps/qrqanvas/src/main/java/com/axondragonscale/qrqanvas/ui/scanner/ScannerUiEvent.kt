package com.axondragonscale.qrqanvas.ui.scanner

/**
 * Created by Ronak Harkhani on 09/07/25
 */
sealed interface ScannerUiEvent {
    data object ToggleFlash : ScannerUiEvent
    data class QRCodeScanned(val rawValue: String) : ScannerUiEvent
    data object UnpauseScanner : ScannerUiEvent
    data class GalleryImagePicked(val uri: String) : ScannerUiEvent
    data object DismissResult : ScannerUiEvent
    data object CopyResult : ScannerUiEvent
    data object ShareResult : ScannerUiEvent
    data object OpenResult : ScannerUiEvent
}
