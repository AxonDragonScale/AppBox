package com.axondragonscale.wallmatic.ui.settings

import com.axondragonscale.commons.UIMode

/**
 * Created by Ronak Harkhani on 09/06/24
 */
internal sealed interface SettingsUiEvent {
    data class UIModeUpdated(val uiMode: UIMode) : SettingsUiEvent
    data class DynamicThemeToggled(val dynamicTheme: Boolean) : SettingsUiEvent
    data class GridSizedUpdated(val gridSize: Int): SettingsUiEvent
    data object NavigateToDevTools : SettingsUiEvent
}
