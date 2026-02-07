package com.axondragonscale.topty.ui.sharetoken

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.axondragonscale.topty.core.TokenUriHandler
import com.axondragonscale.topty.repository.TokenRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import com.axondragonscale.commons.qr.QRGenerator
import kotlinx.coroutines.flow.update

/**
 * Created by Ronak Harkhani on 28/12/24
 */
class ShareTokenVM(
    private val tokenId: Long,
    private val repository: TokenRepository,
) : ViewModel() {

    val uiState = MutableStateFlow(ShareTokenUiState())

    private val uiEffectChannel = Channel<ShareTokenUiEffect>()
    val uiEffect = uiEffectChannel.receiveAsFlow().flowOn(Dispatchers.Main.immediate)

    init {
        loadToken()
    }

    fun onEvent(event: ShareTokenUiEvent): Any = when (event) {
        ShareTokenUiEvent.CopyUri -> onCopyUri()
        ShareTokenUiEvent.ShareUri -> onShareUri()
    }

    private fun loadToken() = viewModelScope.launch(Dispatchers.IO) {
        try {
            val token = repository.getToken(tokenId)
            if (token == null) {
                uiState.value = uiState.value.copy(isLoading = false, error = "Token not found")
                return@launch
            }

            val uri = TokenUriHandler.generateTokenUri(token)
            val qrBitmap = QRGenerator.generateQRCode(uri)

            uiState.update { it.copy(isLoading = false, token = token, tokenUri = uri, qrBitmap = qrBitmap) }
        } catch (e: Exception) {
            uiState.update { it.copy(isLoading = false, error = "Failed to generate QR code: ${e.message}") }
        }
    }

    private fun onCopyUri() = viewModelScope.launch(Dispatchers.IO) {
        uiEffectChannel.send(ShareTokenUiEffect.ShowMessage("URI copied to clipboard"))
    }

    private fun onShareUri() = viewModelScope.launch(Dispatchers.IO) {
        uiEffectChannel.send(ShareTokenUiEffect.ShareUri(uiState.value.tokenUri))
    }
}
