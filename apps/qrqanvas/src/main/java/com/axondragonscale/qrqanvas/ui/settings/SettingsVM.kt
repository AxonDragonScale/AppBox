package com.axondragonscale.qrqanvas.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.axondragonscale.qrqanvas.repository.AppPrefsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Created by Ronak Harkhani on 09/07/25
 */
class SettingsVM(
    private val appPrefsRepository: AppPrefsRepository,
) : ViewModel() {

    val uiState = MutableStateFlow(SettingsUiState())

    init {
        viewModelScope.launch(Dispatchers.IO) {
            combine(
                appPrefsRepository.uiMode.flow,
                appPrefsRepository.dynamicTheme.flow,
            ) { uiMode, dynamicTheme ->
                uiState.update {
                    it.copy(
                        uiMode = uiMode,
                        dynamicTheme = dynamicTheme,
                    )
                }
            }.collect()
        }
    }

    fun onEvent(event: SettingsUiEvent) = viewModelScope.launch(Dispatchers.IO) {
        when (event) {
            is SettingsUiEvent.UIModeUpdated ->
                appPrefsRepository.uiMode.set(event.uiMode)

            is SettingsUiEvent.DynamicThemeToggled ->
                appPrefsRepository.dynamicTheme.set(event.dynamicTheme)

            // Handled in Compose
            SettingsUiEvent.NavigateToDevTools -> Unit
        }
    }
}
