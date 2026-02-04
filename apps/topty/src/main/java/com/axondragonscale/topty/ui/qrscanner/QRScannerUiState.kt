package com.axondragonscale.topty.ui.qrscanner

/**
 * Created by Ronak Harkhani on 12/10/24
 */
data class QRScannerUiState(
    val isLoading: Boolean = true,
    val isFlashEnabled: Boolean = false,
    val isPaused: Boolean = false,
)
