package com.axondragonscale.qrqanvas.ui.settings

import com.axondragonscale.qrqanvas.model.UIMode

/**
 * Created by Ronak Harkhani on 09/07/25
 */
data class SettingsUiState(
    val uiMode: UIMode = UIMode.DEFAULT,
    val dynamicTheme: Boolean = true,
)
