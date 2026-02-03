package com.axondragonscale.topty.ui.qrscanner

import com.google.mlkit.vision.barcode.common.Barcode

/**
 * Created by Ronak Harkhani on 12/10/24
 */
sealed interface QRScannerUiEvent {
    data class QRCodeScanned(val barcodes: List<Barcode>) : QRScannerUiEvent
    data class ToggleFlash(val isFlashEnabled: Boolean) : QRScannerUiEvent
    data object UnpauseScanner: QRScannerUiEvent
}
