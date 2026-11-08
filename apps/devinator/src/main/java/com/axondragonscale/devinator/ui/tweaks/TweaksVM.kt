package com.axondragonscale.devinator.ui.tweaks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.axondragonscale.devinator.model.Tweak
import com.axondragonscale.devinator.model.WriteResult
import com.axondragonscale.devinator.model.writeFailedMessage
import com.axondragonscale.devinator.repository.DevTweaksRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Created by Ronak on 09/11/26 */

/**
 * The page is a projection of the settings database, so the only state it owns is a failure toast.
 * A rejected write is reported and the row keeps showing whatever the database says.
 */
class TweaksVM(
    private val repository: DevTweaksRepository,
) : ViewModel() {

    val uiState: StateFlow<TweaksUiState> = repository.values
        .map(::TweaksUiState)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = TweaksUiState(repository.values.value),
        )

    private val uiEffectChannel = Channel<TweaksUiEffect>()
    val uiEffect = uiEffectChannel.receiveAsFlow().flowOn(Dispatchers.Main.immediate)

    fun onEvent(event: TweaksUiEvent) {
        viewModelScope.launch {
            when (event) {
                is TweaksUiEvent.SetToggle ->
                    report(repository.setToggle(event.tweak, event.enabled), event.tweak.title)

                is TweaksUiEvent.Select ->
                    report(repository.select(event.tweak, event.value), event.tweak.title)

                TweaksUiEvent.Reset -> reportReset(repository.reset())
            }
        }
    }

    private suspend fun report(result: WriteResult, setting: String) {
        if (result is WriteResult.Failed) {
            uiEffectChannel.send(TweaksUiEffect.ShowSnackbar(writeFailedMessage(setting)))
        }
    }

    /** Reset writes every row, so success is the only outcome worth a word and failure may be partial. */
    private suspend fun reportReset(result: WriteResult) {
        uiEffectChannel.send(
            TweaksUiEffect.ShowSnackbar(
                if (result is WriteResult.Failed) {
                    "Couldn't reset every tweak."
                } else {
                    "All tweaks reset to their defaults."
                },
            ),
        )
    }
}

/** Every key's value, keyed so a row can find its own without searching the catalog. */
data class TweaksUiState(
    val values: Map<String, Float> = emptyMap(),
)

sealed interface TweaksUiEvent {
    data class SetToggle(val tweak: Tweak.Toggle, val enabled: Boolean) : TweaksUiEvent
    data class Select(val tweak: Tweak, val value: Float) : TweaksUiEvent
    data object Reset : TweaksUiEvent
}

sealed interface TweaksUiEffect {
    data class ShowSnackbar(val message: String) : TweaksUiEffect
}
