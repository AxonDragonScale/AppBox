package com.axondragonscale.appbox.ui.settings

import android.app.ActivityManager
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.axondragonscale.appbox.data.AppSettings
import com.axondragonscale.appbox.data.AppThemeMode
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SettingsUiState(
    val themeMode: AppThemeMode = AppThemeMode.SYSTEM,
    val isDynamicTheme: Boolean = true
)

class SettingsViewModel(
    private val appSettings: AppSettings,
) : ViewModel() {

    val uiState: StateFlow<SettingsUiState> = combine(
        appSettings.themeMode,
        appSettings.isDynamicTheme
    ) { themeMode, isDynamicTheme ->
        SettingsUiState(
            themeMode = themeMode,
            isDynamicTheme = isDynamicTheme
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SettingsUiState()
    )

    fun setThemeMode(mode: AppThemeMode) {
        viewModelScope.launch {
            appSettings.setThemeMode(mode)
        }
    }

    fun setDynamicTheme(enabled: Boolean) {
        viewModelScope.launch {
            appSettings.setDynamicTheme(enabled)
        }
    }

    fun clearData(context: Context) {
        viewModelScope.launch {
            // Clear settings
            appSettings.clear()

            // Clear app data and kill process
            (context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager)
                .clearApplicationUserData()
        }
    }
}
