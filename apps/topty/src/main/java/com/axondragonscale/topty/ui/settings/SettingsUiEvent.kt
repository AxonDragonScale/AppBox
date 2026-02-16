package com.axondragonscale.topty.ui.settings

import com.axondragonscale.commons.UIMode
import com.axondragonscale.topty.model.CodeDesign
import com.axondragonscale.topty.model.UIFont

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
    data class PreventScreenshotsToggled(val preventScreenshots: Boolean) : SettingsUiEvent
    data object NavigateToBackupRestore : SettingsUiEvent
    data object NavigateToDevTools : SettingsUiEvent
    data object NavigateToChangePassword : SettingsUiEvent
}
