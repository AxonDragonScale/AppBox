package com.axondragonscale.devinator.ui.settings

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Created by Ronak on 11/06/26 */
class SettingsVM : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState = _uiState.asStateFlow()

    fun onEvent(event: SettingsUiEvent): Any = Unit
}

class SettingsUiState

sealed interface SettingsUiEvent
