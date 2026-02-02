package com.axondragonscale.appbox.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.axondragonscale.appbox.data.AppSettings
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class AppViewModel(
    appSettings: AppSettings,
) : ViewModel() {

    val uiState: StateFlow<AppUiState> = combine(
        appSettings.themeMode.flow,
        appSettings.isDynamicTheme.flow
    ) { themeMode, isDynamicTheme ->
        AppUiState(
            themeMode = themeMode,
            isDynamicTheme = isDynamicTheme
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AppUiState()
    )
}
