package com.axondragonscale.qrqanvas.ui.scanner

import android.content.Context
import android.graphics.BitmapFactory
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.axondragonscale.qrqanvas.core.QRContentCodec
import com.axondragonscale.qrqanvas.database.entity.ScanHistoryEntity
import com.axondragonscale.qrqanvas.repository.ScanHistoryRepository
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


/**
 * Created by Ronak Harkhani on 09/07/25
 */
class ScannerVM(
    private val context: Context,
    private val scanHistoryRepository: ScanHistoryRepository,
) : ViewModel() {

    val uiState = MutableStateFlow(ScannerUiState())

    private val uiEffectChannel = Channel<ScannerUiEffect>(Channel.BUFFERED)
    val uiEffect = uiEffectChannel.receiveAsFlow().flowOn(Dispatchers.Main.immediate)

    fun onEvent(event: ScannerUiEvent): Unit = when (event) {
        is ScannerUiEvent.ToggleFlash -> onToggleFlash()
        is ScannerUiEvent.QRCodeScanned -> onQRCodeScanned(event.rawValue)
        is ScannerUiEvent.UnpauseScanner -> onUnpauseScanner()
        is ScannerUiEvent.GalleryImagePicked -> onGalleryImagePicked(event.uri)
        is ScannerUiEvent.DismissResult -> onDismissResult()
        is ScannerUiEvent.CopyResult -> onCopyResult()
        is ScannerUiEvent.ShareResult -> onShareResult()
        is ScannerUiEvent.OpenResult -> onOpenResult()
    }

    private fun onToggleFlash() {
        uiState.update { it.copy(isFlashEnabled = !it.isFlashEnabled) }
    }

    private fun onQRCodeScanned(rawValue: String) {
        // Don't process if already showing a result
        if (uiState.value.showResultSheet) return

        val content = QRContentCodec.decode(rawValue)
        uiState.update {
            it.copy(
                isScanPaused = true,
                scannedContent = content,
                scannedRawValue = rawValue,
                showResultSheet = true,
            )
        }

        // Auto-save to scan history
        viewModelScope.launch(Dispatchers.IO) {
            scanHistoryRepository.insert(
                ScanHistoryEntity(
                    rawValue = rawValue,
                    content = content,
                    scannedAt = System.currentTimeMillis(),
                )
            )
        }
    }

    private fun onUnpauseScanner() {
        uiState.update { it.copy(isScanPaused = false) }
    }

    private fun onGalleryImagePicked(uri: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val inputStream = context.contentResolver.openInputStream(
                    android.net.Uri.parse(uri)
                ) ?: run {
                    uiEffectChannel.send(ScannerUiEffect.ShowSnackbar("Failed to read image"))
                    return@launch
                }

                val bitmap = BitmapFactory.decodeStream(inputStream)
                inputStream.close()

                if (bitmap == null) {
                    uiEffectChannel.send(ScannerUiEffect.ShowSnackbar("Failed to decode image"))
                    return@launch
                }

                val inputImage = InputImage.fromBitmap(bitmap, 0)
                val options = BarcodeScannerOptions.Builder()
                    .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
                    .build()
                val scanner = BarcodeScanning.getClient(options)

                scanner.process(inputImage)
                    .addOnSuccessListener { barcodes ->
                        val rawValue = barcodes.firstOrNull()?.rawValue
                        if (rawValue != null) {
                            onQRCodeScanned(rawValue)
                        } else {
                            viewModelScope.launch {
                                uiEffectChannel.send(
                                    ScannerUiEffect.ShowSnackbar("No QR code found in image")
                                )
                            }
                        }
                    }
                    .addOnFailureListener {
                        viewModelScope.launch {
                            uiEffectChannel.send(
                                ScannerUiEffect.ShowSnackbar("Failed to scan image")
                            )
                        }
                    }
            } catch (e: Exception) {
                uiEffectChannel.send(ScannerUiEffect.ShowSnackbar("Failed to process image"))
            }
        }
    }

    private fun onDismissResult() {
        uiState.update {
            it.copy(
                showResultSheet = false,
                scannedContent = null,
                scannedRawValue = null,
                isScanPaused = false,
            )
        }
    }

    private fun onCopyResult() {
        val rawValue = uiState.value.scannedRawValue ?: return
        viewModelScope.launch {
            uiEffectChannel.send(ScannerUiEffect.CopyToClipboard(rawValue))
        }
    }

    private fun onShareResult() {
        val rawValue = uiState.value.scannedRawValue ?: return
        viewModelScope.launch {
            uiEffectChannel.send(ScannerUiEffect.ShareText(rawValue))
        }
    }

    private fun onOpenResult() {
        val rawValue = uiState.value.scannedRawValue ?: return
        viewModelScope.launch {
            uiEffectChannel.send(ScannerUiEffect.OpenUrl(rawValue))
        }
    }
}
