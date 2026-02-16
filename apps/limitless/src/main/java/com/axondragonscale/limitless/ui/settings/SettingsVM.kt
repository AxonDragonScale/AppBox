package com.axondragonscale.limitless.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.axondragonscale.limitless.repository.AppPrefsRepository
import com.axondragonscale.limitless.util.DevTools
import com.axondragonscale.limitless.util.collect
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Created by Ronak Harkhani on 01/11/24
 */
class SettingsVM(
    private val appPrefsRepository: AppPrefsRepository,
    private val devTools: DevTools
): ViewModel() {

    val uiState = MutableStateFlow(SettingsUiState())

    init {
        viewModelScope.launch(Dispatchers.IO) {
            combine(
                appPrefsRepository.uiModeFlow,
                appPrefsRepository.dynamicThemeFlow
            ) { uiMode, dynamicTheme ->
                uiState.update {
                    it.copy(
                        uiMode = uiMode,
                        dynamicTheme = dynamicTheme
                    )
                }
            }.collect()
        }
    }

    fun onEvent(event: SettingsUiEvent): Any = when (event) {
        is SettingsUiEvent.UIModeUpdated -> onUiModeUpdated(event)
        is SettingsUiEvent.DynamicThemeToggled -> onDynamicThemeToggled(event)
        SettingsUiEvent.ClearData -> onClearData()

        // Handled in compose
        SettingsUiEvent.NavigateBack -> Unit
    }

    private fun onUiModeUpdated(
        event: SettingsUiEvent.UIModeUpdated
    ) = viewModelScope.launch(Dispatchers.IO) {
        appPrefsRepository.setUiMode(event.uiMode)
    }

    private fun onDynamicThemeToggled(
        event: SettingsUiEvent.DynamicThemeToggled
    ) = viewModelScope.launch(Dispatchers.IO) {
        appPrefsRepository.setDynamicTheme(event.dynamicTheme)
    }

    private fun onClearData() = viewModelScope.launch(Dispatchers.IO) {
        devTools.clearData()
    }

}
