package com.axondragonscale.npinner.ui.notifications.themePicker

import com.axondragonscale.npinner.model.DarkModeConfig

/**
 * Created by Ronak Harkhani on 14/05/23
 */
sealed interface ThemePickerUiState {
    
    object Loading : ThemePickerUiState
    
    data class Success(
        val darkModeConfig: DarkModeConfig,
    ) : ThemePickerUiState
    
}
