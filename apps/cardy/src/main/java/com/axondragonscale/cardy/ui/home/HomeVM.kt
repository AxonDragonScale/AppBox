/** Created by Ronak on 05/30/26 */
package com.axondragonscale.cardy.ui.home

import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.axondragonscale.cardy.model.CardReadResultHolder
import com.axondragonscale.cardy.nfc.CardReader
import com.axondragonscale.cardy.nfc.NfcManager
import com.axondragonscale.commons.logD
import com.axondragonscale.commons.logE
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
    private val cardReadResultHolder: CardReadResultHolder,
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

    // region Event Handling

    fun onEvent(event: HomeUiEvent): Any = when (event) {
        is HomeUiEvent.RefreshNfcStatus -> refreshNfcStatus()
        is HomeUiEvent.HandleNfcIntent -> handleNfcIntent(event.intent)
        is HomeUiEvent.NavigateToVault -> Unit // Handled in composable
        is HomeUiEvent.NavigateToSettings -> Unit // Handled in composable
        is HomeUiEvent.OpenNfcSettings -> Unit // Handled in composable
    }.also { TAG.logD { "onEvent: $event" } }

    // endregion

    private fun refreshNfcStatus() {
        _uiState.update {
            it.copy(
                isNfcSupported = nfcManager.isNfcSupported,
                isNfcEnabled = nfcManager.isNfcEnabled,
            )
        }
        TAG.logD { "refreshNfcStatus: supported=${nfcManager.isNfcSupported}, enabled=${nfcManager.isNfcEnabled}" }
    }

    private fun handleNfcIntent(intent: Intent) = viewModelScope.launch(Dispatchers.IO) {
        TAG.logD { "handleNfcIntent: action=${intent.action}, extras=${intent.extras}" }

        val isoDep = nfcManager.getIsoDep(intent) ?: run {
            TAG.logD { "handleNfcIntent: getIsoDep returned null, aborting" }
            return@launch
        }

        TAG.logD { "handleNfcIntent: isReading=true" }
        _uiState.update { it.copy(isReading = true) }

        val result = cardReader.readCard(isoDep)

        result.onSuccess { cardReadResult ->
            TAG.logD { "handleNfcIntent: readCard success — network=${cardReadResult.primaryNetwork}, pan_last4=${cardReadResult.primaryPan?.takeLast(4)}" }
            _uiState.update { state ->
                state.copy(isReading = false, readError = null)
            }
            cardReadResultHolder.result = cardReadResult
            TAG.logD { "handleNfcIntent: sending NavigateToCardDetail effect" }
            uiEffectChannel.send(HomeUiEffect.NavigateToCardDetail)
        }

        result.onFailure { error ->
            TAG.logE { "handleNfcIntent: readCard failure — ${error.message}" }
            _uiState.update {
                it.copy(isReading = false, readError = error.message ?: "Failed to read card")
            }
        }
    }
}

data class HomeUiState(
    val isNfcSupported: Boolean = true,
    val isNfcEnabled: Boolean = true,
    val isReading: Boolean = false,
    val readError: String? = null,
)

sealed interface HomeUiEvent {
    data class HandleNfcIntent(val intent: Intent) : HomeUiEvent
    data object RefreshNfcStatus : HomeUiEvent
    data object NavigateToVault : HomeUiEvent
    data object NavigateToSettings : HomeUiEvent
    data object OpenNfcSettings : HomeUiEvent
}

sealed interface HomeUiEffect {
    data object NavigateToCardDetail : HomeUiEffect
}
