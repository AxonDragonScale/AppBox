package com.axondragonscale.topty.ui.settings

import com.axondragonscale.topty.model.CodeDesign
import com.axondragonscale.topty.model.UIFont
import com.axondragonscale.topty.model.UIMode

/**
 * Created by Ronak Harkhani on 13/10/24
 */
sealed interface SettingsUiEvent {
    data object NavigateBack : SettingsUiEvent
    data class UIModeUpdated(val uiMode: UIMode) : SettingsUiEvent
    data class DynamicThemeToggled(val dynamicTheme: Boolean) : SettingsUiEvent
    data class UIFontUpdated(val uiFont: UIFont) : SettingsUiEvent
    data class CodeDesignUpdated(val codeDesign: CodeDesign) : SettingsUiEvent
    data class SpeakOutOnTapToggled(val speakOutOnTap: Boolean) : SettingsUiEvent
    data class BiometricLockToggled(val biometricLock: Boolean) : SettingsUiEvent
    data object NavigateToDevTools : SettingsUiEvent
}
