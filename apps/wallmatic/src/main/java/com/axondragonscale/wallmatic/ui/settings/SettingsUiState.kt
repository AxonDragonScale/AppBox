package com.axondragonscale.wallmatic.ui.settings

import com.axondragonscale.commons.UIMode

/**
 * Created by Ronak Harkhani on 09/06/24
 */
internal data class SettingsUiState(
    val uiMode: UIMode = UIMode.AUTO,
    val dynamicTheme: Boolean = false,
    val gridSize: Int = 2,
)
