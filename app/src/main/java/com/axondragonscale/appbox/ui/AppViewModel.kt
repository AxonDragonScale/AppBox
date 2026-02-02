package com.axondragonscale.appbox.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.axondragonscale.appbox.data.AppSettings
import com.axondragonscale.appbox.data.AppThemeMode
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class AppUiState(
    val themeMode: AppThemeMode = AppThemeMode.SYSTEM,
    val isDynamicTheme: Boolean = true
)

class AppViewModel(
    appSettings: AppSettings,
) : ViewModel() {

    val uiState: StateFlow<AppUiState> = combine(
        appSettings.themeMode,
        appSettings.isDynamicTheme
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
