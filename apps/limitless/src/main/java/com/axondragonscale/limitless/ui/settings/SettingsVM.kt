package com.axondragonscale.limitless.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.axondragonscale.commons.collect
import com.axondragonscale.limitless.repository.AppPrefsRepository
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
): ViewModel() {

    val uiState = MutableStateFlow(SettingsUiState())

    init {
        viewModelScope.launch(Dispatchers.IO) {
            combine(
                appPrefsRepository.uiMode.flow,
                appPrefsRepository.dynamicTheme.flow
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

        // Handled in compose
        SettingsUiEvent.NavigateBack -> Unit
        SettingsUiEvent.NavigateToDevTools -> Unit
    }

    private fun onUiModeUpdated(
        event: SettingsUiEvent.UIModeUpdated
    ) = viewModelScope.launch(Dispatchers.IO) {
        appPrefsRepository.uiMode.set(event.uiMode)
    }

    private fun onDynamicThemeToggled(
        event: SettingsUiEvent.DynamicThemeToggled
    ) = viewModelScope.launch(Dispatchers.IO) {
        appPrefsRepository.dynamicTheme.set(event.dynamicTheme)
    }

}
