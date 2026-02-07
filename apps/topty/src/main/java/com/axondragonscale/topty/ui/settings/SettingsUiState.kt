package com.axondragonscale.topty.ui.settings

import com.axondragonscale.topty.model.CodeDesign
import com.axondragonscale.topty.model.UIFont
import com.axondragonscale.topty.model.UIMode

/**
 * Created by Ronak Harkhani on 13/10/24
 */
data class SettingsUiState(
    val uiMode: UIMode = UIMode.DEFAULT,
    val dynamicTheme: Boolean = false,
    val uiFont: UIFont = UIFont.DEFAULT,
    val codeDesign: CodeDesign = CodeDesign.DEFAULT,
    val speakOutOnTap: Boolean = false,
    val biometricLock: Boolean = false,
    val biometricUnavailableReason: String? = null,
    val preventScreenshots: Boolean = true,
)
