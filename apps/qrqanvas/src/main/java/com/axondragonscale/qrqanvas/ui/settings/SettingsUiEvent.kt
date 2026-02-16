package com.axondragonscale.qrqanvas.ui.settings

import com.axondragonscale.commons.UIMode

/**
 * Created by Ronak Harkhani on 09/07/25
 */
sealed interface SettingsUiEvent {
    data class UIModeUpdated(val uiMode: UIMode) : SettingsUiEvent
    data class DynamicThemeToggled(val dynamicTheme: Boolean) : SettingsUiEvent
    data object NavigateToDevTools : SettingsUiEvent
}
