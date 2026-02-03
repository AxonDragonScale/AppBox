package com.axondragonscale.topty.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.axondragonscale.topty.core.QRScanner
import com.axondragonscale.topty.core.TTS
import com.axondragonscale.topty.core.TokenUriHandler
import com.axondragonscale.topty.model.Protocol
import com.axondragonscale.topty.repository.AppPrefsRepository
import com.axondragonscale.topty.repository.TokenRepository
import com.axondragonscale.topty.util.collect
import com.axondragonscale.topty.util.generateCode
import com.google.mlkit.vision.common.InputImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Created by Ronak Harkhani on 22/09/24
 */
class HomeVM(
    private val appPrefsRepository: AppPrefsRepository,
    private val repository: TokenRepository,
    private val tts: TTS,
) : ViewModel() {

    val uiState = MutableStateFlow(HomeUiState())

    private val uiEffectChannel = Channel<HomeUiEffect>()
    val uiEffect = uiEffectChannel.receiveAsFlow().flowOn(Dispatchers.Main.immediate)

    init {
        viewModelScope.launch(Dispatchers.IO) {
            combine(
                appPrefsRepository.codeDesignFlow,
                appPrefsRepository.speakOutOnTapFlow,
                repository.getTokens(),
            ) { codeDesign, speakOutOnTap, tokens ->
                val codes = tokens.map { token -> token.generateCode() }
                uiState.update {
                    it.copy(
                        isLoading = false,
                        codeDesign = codeDesign,
                        codes = codes,
                        speakOutOnTap = speakOutOnTap,
                    )
                }
            }.collect()
        }
    }

    fun onEvent(event: HomeUiEvent): Any = when (event) {
        is HomeUiEvent.ScanImage -> onScanImage(event)
        is HomeUiEvent.RefreshCode -> onRefreshCode(event)
        is HomeUiEvent.SpeakOut -> onSpeakOut(event)

        // Handled in Compose
        HomeUiEvent.NavigateToAddToken -> Unit
        is HomeUiEvent.NavigateToEditToken -> Unit
        HomeUiEvent.NavigateToQRScanner -> Unit
        HomeUiEvent.NavigateToSettings -> Unit
    }

    private fun onScanImage(
        event: HomeUiEvent.ScanImage,
    ) = viewModelScope.launch(Dispatchers.IO) {
        QRScanner.scan(InputImage.fromFilePath(event.context, event.uri)) { barcodes ->
            viewModelScope.launch(Dispatchers.IO) {
                val qrCode = barcodes?.firstOrNull { TokenUriHandler.isValidTokenUri(it.rawValue) }
                if (qrCode != null)
                    uiEffectChannel.send(HomeUiEffect.NavigateToAddToken(qrCode.rawValue!!))
                else
                    uiEffectChannel.send(HomeUiEffect.ShowNoQRFoundDialog)
            }
        }
    }

    private fun onRefreshCode(
        event: HomeUiEvent.RefreshCode,
    ) = viewModelScope.launch(Dispatchers.IO) {
        // Update counter if token is HOTP, otherwise just generate new token
        var token = event.code.token
        if (token.protocol == Protocol.HOTP) {
            token = token.copy(counter = token.counter + 1)
            repository.saveToken(token)
        }
        uiState.update {
            val newCode = token.generateCode()
            val newCodes = it.codes.toMutableList()
            newCodes[event.index] = newCode
            it.copy(codes = newCodes)
        }
    }

    private fun onSpeakOut(
        event: HomeUiEvent.SpeakOut,
    ) = viewModelScope.launch(Dispatchers.IO) {
        tts.speak(code = event.code.code)
    }

    override fun onCleared() {
        tts.release()
        super.onCleared()
    }

}
