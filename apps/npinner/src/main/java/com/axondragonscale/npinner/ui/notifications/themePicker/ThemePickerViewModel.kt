package com.axondragonscale.npinner.ui.notifications.themePicker

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.axondragonscale.npinner.model.DarkModeConfig
import com.axondragonscale.npinner.repository.AppPrefsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Created by Ronak Harkhani on 14/05/23
 */

class ThemePickerViewModel(
    private val repository: AppPrefsRepository,
) : ViewModel() {

    val uiState: StateFlow<ThemePickerUiState> =
        themePickerUiState()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = ThemePickerUiState.Loading,
            )


    fun onDarkModeConfigChange(darkModeConfig: DarkModeConfig) = viewModelScope.launch {
        repository.setDarkModeConfig(darkModeConfig)
    }

    private fun themePickerUiState(): Flow<ThemePickerUiState> =
        repository.darkModeConfigFlow.map { ThemePickerUiState.Success(it) }

}
