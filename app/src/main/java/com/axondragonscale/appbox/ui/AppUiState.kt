package com.axondragonscale.appbox.ui

import com.axondragonscale.appbox.model.AppThemeMode

data class AppUiState(
    val themeMode: AppThemeMode = AppThemeMode.SYSTEM,
    val isDynamicTheme: Boolean = true
)