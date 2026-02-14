package com.axondragonscale.qrqanvas.ui.scanner

import android.app.Application
import android.graphics.BitmapFactory
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.axondragonscale.qrqanvas.core.QRContentParser
import com.axondragonscale.qrqanvas.repository.ScanHistoryRepository
import com.axondragonscale.qrqanvas.database.entity.ScanHistoryEntity
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Created by Ronak Harkhani on 09/07/25
 */
class ScannerVM(
    private val application: Application,
    private val qrContentParser: QRContentParser,
    private val scanHistoryRepository: ScanHistoryRepository,
    private val json: Json,
) : ViewModel() {

    val uiState = MutableStateFlow(ScannerUiState())

    private val _effects = Channel<ScannerUiEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

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

        val content = qrContentParser.parse(rawValue)
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
            val contentJson = json.encodeToString(content)
            scanHistoryRepository.insert(
                ScanHistoryEntity(
                    rawValue = rawValue,
                    contentJson = contentJson,
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
                val inputStream = application.contentResolver.openInputStream(
                    android.net.Uri.parse(uri)
                ) ?: run {
                    _effects.send(ScannerUiEffect.ShowSnackbar("Failed to read image"))
                    return@launch
                }

                val bitmap = BitmapFactory.decodeStream(inputStream)
                inputStream.close()

                if (bitmap == null) {
                    _effects.send(ScannerUiEffect.ShowSnackbar("Failed to decode image"))
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
                                _effects.send(
                                    ScannerUiEffect.ShowSnackbar("No QR code found in image")
                                )
                            }
                        }
                    }
                    .addOnFailureListener {
                        viewModelScope.launch {
                            _effects.send(
                                ScannerUiEffect.ShowSnackbar("Failed to scan image")
                            )
                        }
                    }
            } catch (e: Exception) {
                _effects.send(ScannerUiEffect.ShowSnackbar("Failed to process image"))
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
            _effects.send(ScannerUiEffect.CopyToClipboard(rawValue))
        }
    }

    private fun onShareResult() {
        val rawValue = uiState.value.scannedRawValue ?: return
        viewModelScope.launch {
            _effects.send(ScannerUiEffect.ShareText(rawValue))
        }
    }

    private fun onOpenResult() {
        val rawValue = uiState.value.scannedRawValue ?: return
        viewModelScope.launch {
            _effects.send(ScannerUiEffect.OpenUrl(rawValue))
        }
    }
}
