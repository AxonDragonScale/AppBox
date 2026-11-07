package com.axondragonscale.devinator.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.axondragonscale.devinator.model.AdbEndpoint
import com.axondragonscale.devinator.model.AutoOffDuration
import com.axondragonscale.devinator.model.WriteResult
import com.axondragonscale.devinator.repository.DevSettingsRepository
import kotlin.time.Instant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Created by Ronak on 11/06/26 */
class HomeVM(
    private val repository: DevSettingsRepository,
) : ViewModel() {

    private val pendingWireless = MutableStateFlow(false)

    val uiState: StateFlow<HomeUiState> = combine(
        repository.state,
        repository.endpoints,
        repository.autoOffDuration,
        repository.autoOffDeadline,
        pendingWireless,
    ) { dev, endpoints, autoOff, deadline, pending ->
        HomeUiState(
            developerOptions = dev.developerOptions,
            usbDebugging = dev.usbDebugging,
            wirelessDebugging = dev.wirelessDebugging,
            wirelessPending = pending,
            endpoint = endpoints.firstOrNull(),
            autoOffDuration = autoOff,
            autoOffDeadline = deadline,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HomeUiState(
            developerOptions = repository.state.value.developerOptions,
            usbDebugging = repository.state.value.usbDebugging,
            wirelessDebugging = repository.state.value.wirelessDebugging,
            endpoint = repository.endpoints.value.firstOrNull(),
            autoOffDuration = repository.autoOffDuration.value,
            autoOffDeadline = repository.autoOffDeadline.value,
        ),
    )

    private val uiEffectChannel = Channel<HomeUiEffect>()
    val uiEffect = uiEffectChannel.receiveAsFlow().flowOn(Dispatchers.Main.immediate)

    fun onEvent(event: HomeUiEvent): Any = when (event) {
        is HomeUiEvent.SetDeveloperOptions -> setDeveloperOptions(event.enabled)
        is HomeUiEvent.SetUsbDebugging -> setUsbDebugging(event.enabled)
        is HomeUiEvent.SetWirelessDebugging -> setWirelessDebugging(event.enabled)
        is HomeUiEvent.SelectAutoOff -> selectAutoOff(event.duration)
        HomeUiEvent.OpenSettings -> Unit
    }

    private fun setDeveloperOptions(enabled: Boolean) = viewModelScope.launch {
        reportFailure(repository.setDeveloperOptions(enabled))
    }

    private fun setUsbDebugging(enabled: Boolean) = viewModelScope.launch {
        reportFailure(repository.setUsbDebugging(enabled))
    }

    private fun setWirelessDebugging(enabled: Boolean) = viewModelScope.launch {
        if (!enabled) {
            reportFailure(repository.setWirelessDebugging(false))
            return@launch
        }
        pendingWireless.value = true
        try {
            when (val result = repository.setWirelessDebugging(true)) {
                is WriteResult.Failed -> reportFailure(result)
                WriteResult.AwaitingConsent ->
                    uiEffectChannel.send(HomeUiEffect.ShowSnackbar("Wireless debugging not confirmed"))
                WriteResult.Ok -> Unit
            }
        } finally {
            pendingWireless.value = false
        }
    }

    private fun selectAutoOff(duration: AutoOffDuration) = viewModelScope.launch {
        repository.setAutoOffDuration(duration)
    }

    private suspend fun reportFailure(result: WriteResult) {
        if (result is WriteResult.Failed) {
            uiEffectChannel.send(HomeUiEffect.ShowSnackbar("Couldn't change this setting."))
        }
    }
}

data class HomeUiState(
    val developerOptions: Boolean = false,
    val usbDebugging: Boolean = false,
    val wirelessDebugging: Boolean = false,
    val wirelessPending: Boolean = false,
    val endpoint: AdbEndpoint? = null,
    val autoOffDuration: AutoOffDuration = AutoOffDuration.OFF,
    val autoOffDeadline: Instant? = null,
)

sealed interface HomeUiEvent {
    data class SetDeveloperOptions(val enabled: Boolean) : HomeUiEvent
    data class SetUsbDebugging(val enabled: Boolean) : HomeUiEvent
    data class SetWirelessDebugging(val enabled: Boolean) : HomeUiEvent
    data class SelectAutoOff(val duration: AutoOffDuration) : HomeUiEvent
    data object OpenSettings : HomeUiEvent
}

sealed interface HomeUiEffect {
    data class ShowSnackbar(val message: String) : HomeUiEffect
}
