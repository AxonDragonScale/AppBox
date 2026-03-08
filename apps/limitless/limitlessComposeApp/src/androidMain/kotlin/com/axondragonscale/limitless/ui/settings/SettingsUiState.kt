package com.axondragonscale.limitless.ui.settings

import com.axondragonscale.commons.UIMode

/**
 * Created by Ronak Harkhani on 01/11/24
 */
data class SettingsUiState(
    val uiMode: UIMode = UIMode.DEFAULT,
    val dynamicTheme: Boolean = false,
    val highlightCurrentDay: Boolean = true,
)
