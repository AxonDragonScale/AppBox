package com.axondragonscale.npinner.ui.notifications.themePicker

import com.axondragonscale.commons.UIMode

/**
 * Created by Ronak Harkhani on 14/05/23
 */
sealed interface ThemePickerUiState {

    object Loading : ThemePickerUiState

    data class Success(
        val uiMode: UIMode,
    ) : ThemePickerUiState

}
