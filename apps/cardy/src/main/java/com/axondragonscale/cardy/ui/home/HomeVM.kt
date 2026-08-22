/** Created by Ronak on 05/30/26 */
package com.axondragonscale.cardy.ui.home

import android.nfc.Tag
import android.nfc.TagLostException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.axondragonscale.cardy.nfc.CardReadException
import com.axondragonscale.cardy.nfc.CardReader
import com.axondragonscale.cardy.nfc.NfcManager
import com.axondragonscale.cardy.repository.CardRepository
import com.axondragonscale.commons.logD
import com.axondragonscale.commons.logE
import java.io.IOException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val TAG = "HomeVM"

class HomeVM(
    private val nfcManager: NfcManager,
    private val cardReader: CardReader,
    private val cardRepository: CardRepository,
) : ViewModel() {

    // region State

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState = _uiState.asStateFlow()

    private val uiEffectChannel = Channel<HomeUiEffect>()
    val uiEffect = uiEffectChannel.receiveAsFlow().flowOn(Dispatchers.Main.immediate)

    // endregion

    init {
        TAG.logD { "HomeVM created, instance=${hashCode()}" }
        refreshNfcStatus()
        observeNfcState()
    }

    private fun observeNfcState() {
        viewModelScope.launch {
            nfcManager.nfcEnabledState.collect { isEnabled ->
                TAG.logD { "HomeVM: nfcEnabledState collected -> $isEnabled" }
                _uiState.update {
                    it.copy(
                        isNfcSupported = nfcManager.isNfcSupported,
                        isNfcEnabled = isEnabled,
                    )
                }
            }
        }
    }

    private fun refreshNfcStatus() {
        _uiState.update {
            it.copy(
                isNfcSupported = nfcManager.isNfcSupported,
                isNfcEnabled = nfcManager.isNfcEnabled,
            )
        }
        TAG.logD { "refreshNfcStatus: supported=${nfcManager.isNfcSupported}, enabled=${nfcManager.isNfcEnabled}" }
    }

    fun readTag(tag: Tag) = viewModelScope.launch(Dispatchers.IO) {
        val isoDep = nfcManager.getIsoDep(tag) ?: run {
            TAG.logD { "readTag: getIsoDep returned null, aborting" }
            return@launch
        }

        TAG.logD { "readTag: isReading=true" }
        _uiState.update { it.copy(isReading = true) }

        val result = cardReader.readCard(isoDep)

        result.onSuccess { cardReadResult ->
            TAG.logD { "readTag: readCard success — network=${cardReadResult.primaryNetwork}, pan_last4=${cardReadResult.primaryPan?.takeLast(4)}" }
            _uiState.update { state ->
                state.copy(isReading = false, readError = null)
            }
            cardRepository.setPendingRead(cardReadResult)
            TAG.logD { "readTag: sending NavigateToCardDetail effect" }
            uiEffectChannel.send(HomeUiEffect.NavigateToCardDetail)
        }

        result.onFailure { error ->
            TAG.logE { "readTag: readCard failure — ${error.message}" }
            _uiState.update {
                it.copy(isReading = false, readError = error.toUserMessage())
            }
        }
    }
}

internal fun Throwable.toUserMessage(): String = when (this) {
    is TagLostException ->
        "Card moved too soon. Hold it against your phone until reading finishes."
    is CardReadException.UnsupportedCard ->
        "This card does not expose a supported contactless payment application."
    is CardReadException.UnreadableCard ->
        "Card detected, but its payment data could not be read. Hold it steady and try again."
    is IOException ->
        "Could not communicate with the card. Hold it steady and try again."
    else ->
        "Could not read this card. Hold it steady and try again."
}

data class HomeUiState(
    val isNfcSupported: Boolean = true,
    val isNfcEnabled: Boolean = true,
    val isReading: Boolean = false,
    val readError: String? = null,
)

sealed interface HomeUiEffect {
    data object NavigateToCardDetail : HomeUiEffect
}
