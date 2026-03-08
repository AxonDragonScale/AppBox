package com.axondragonscale.jest.ui.settings

import com.axondragonscale.commons.UIMode

/**
 * Created by Ronak Harkhani on 09/06/24
 */

data class SettingsUiState(
    val uiMode: UIMode = UIMode.DEFAULT,
    val dynamicTheme: Boolean = true,
)
