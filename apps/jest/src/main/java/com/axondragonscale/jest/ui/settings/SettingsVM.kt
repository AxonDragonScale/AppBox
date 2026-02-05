package com.axondragonscale.jest.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.axondragonscale.jest.repository.PrefsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Created by Ronak Harkhani on 09/06/24
 */
class SettingsVM(
    private val prefsRepository: PrefsRepository,
) : ViewModel() {

    val uiState = MutableStateFlow(SettingsUiState())

    init {
        viewModelScope.launch(Dispatchers.IO) {
            combine(
                prefsRepository.uiMode.flow,
                prefsRepository.dynamicTheme.flow,
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
                prefsRepository.uiMode.set(event.uiMode)

            is SettingsUiEvent.DynamicThemeToggled ->
                prefsRepository.dynamicTheme.set(event.dynamicTheme)

            // Handled in Compose
            SettingsUiEvent.NavigateToDevTools -> Unit
        }
    }
}
