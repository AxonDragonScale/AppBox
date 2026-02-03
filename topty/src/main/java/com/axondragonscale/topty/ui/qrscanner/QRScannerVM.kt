package com.axondragonscale.topty.ui.qrscanner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.axondragonscale.topty.core.TokenUriHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Created by Ronak Harkhani on 12/10/24
 */
class QRScannerVM: ViewModel() {

    val uiState = MutableStateFlow(QRScannerUiState())

    private val uiEffectChannel = Channel<QRScannerUiEffect>()
    val uiEffect = uiEffectChannel.receiveAsFlow().flowOn(Dispatchers.Main.immediate)

    fun onEvent(event: QRScannerUiEvent) {
        when (event) {
            is QRScannerUiEvent.QRCodeScanned -> onQRCodeScanned(event)
            is QRScannerUiEvent.ToggleFlash -> onToggleFlash(event)
            is QRScannerUiEvent.UnpauseScanner -> onUnpauseScanner()
        }
    }

    private fun onQRCodeScanned(
        event: QRScannerUiEvent.QRCodeScanned
    ) = viewModelScope.launch(Dispatchers.IO) {
        uiState.update { it.copy(isPaused = true) }
        val qrCode = event.barcodes.firstOrNull { TokenUriHandler.isValidTokenUri(it.rawValue) }

        if (qrCode != null)
            uiEffectChannel.send(QRScannerUiEffect.NavigateToAddToken(qrCode.rawValue!!))
        else
            uiState.update { it.copy(isPaused = false) }
    }

    private fun onToggleFlash(event: QRScannerUiEvent.ToggleFlash) {
        uiState.update { it.copy(isFlashEnabled = event.isFlashEnabled) }
    }

    private fun onUnpauseScanner() {
        uiState.update { it.copy(isPaused = false) }
    }
}
