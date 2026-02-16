package com.axondragonscale.limitless.ui.settings

import com.axondragonscale.commons.UIMode

/**
 * Created by Ronak Harkhani on 01/11/24
 */
sealed interface SettingsUiEvent {
    data object NavigateBack : SettingsUiEvent
    data class UIModeUpdated(val uiMode: UIMode) : SettingsUiEvent
    data class DynamicThemeToggled(val dynamicTheme: Boolean) : SettingsUiEvent
    data object NavigateToDevTools : SettingsUiEvent
}
