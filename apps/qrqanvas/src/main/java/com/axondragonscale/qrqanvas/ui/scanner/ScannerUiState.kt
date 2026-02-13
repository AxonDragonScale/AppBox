package com.axondragonscale.qrqanvas.ui.scanner

import com.axondragonscale.qrqanvas.model.QRContent

/**
 * Created by Ronak Harkhani on 09/07/25
 */
data class ScannerUiState(
    val isFlashEnabled: Boolean = false,
    val isScanPaused: Boolean = false,
    val scannedContent: QRContent? = null,
    val scannedRawValue: String? = null,
    val showResultSheet: Boolean = false,
)
