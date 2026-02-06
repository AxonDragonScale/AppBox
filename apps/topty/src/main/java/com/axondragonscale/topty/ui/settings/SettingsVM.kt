package com.axondragonscale.topty.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.axondragonscale.commons.Tuple6
import com.axondragonscale.commons.collect
import com.axondragonscale.commons.combine
import com.axondragonscale.topty.repository.ToptyPrefsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Created by Ronak Harkhani on 13/10/24
 */
class SettingsVM(
    private val prefsRepository: ToptyPrefsRepository,
): ViewModel() {

    val uiState = MutableStateFlow(SettingsUiState())

    init {
        viewModelScope.launch(Dispatchers.IO) {
            combine(
                prefsRepository.uiMode.flow,
                prefsRepository.dynamicTheme.flow,
                prefsRepository.uiFont.flow,
                prefsRepository.codeDesign.flow,
                prefsRepository.speakOutOnTap.flow,
                prefsRepository.biometricLock.flow,
                prefsRepository.preventScreenshots.flow,
            ) { uiMode, dynamicTheme, uiFont, codeDesign, speakOutOnTap, biometricLock, preventScreenshots ->
                uiState.update {
                    it.copy(
                        uiMode = uiMode,
                        dynamicTheme = dynamicTheme,
                        uiFont = uiFont,
                        codeDesign = codeDesign,
                        speakOutOnTap = speakOutOnTap,
                        biometricLock = biometricLock,
                        preventScreenshots = preventScreenshots,
                    )
                }
            }.collect()
        }
    }

    fun onEvent(event: SettingsUiEvent) = viewModelScope.launch(Dispatchers.IO) {
        when (event) {
            is SettingsUiEvent.UIModeUpdated ->
                prefsRepository.uiMode.set(event.uiMode)

            is SettingsUiEvent.DynamicThemeToggled ->
                prefsRepository.dynamicTheme.set(event.dynamicTheme)

            is SettingsUiEvent.UIFontUpdated ->
                prefsRepository.uiFont.set(event.uiFont)

            is SettingsUiEvent.CodeDesignUpdated ->
                prefsRepository.codeDesign.set(event.codeDesign)

            is SettingsUiEvent.SpeakOutOnTapToggled ->
                prefsRepository.speakOutOnTap.set(event.speakOutOnTap)

            is SettingsUiEvent.BiometricLockToggled ->
                prefsRepository.biometricLock.set(event.biometricLock)

            is SettingsUiEvent.PreventScreenshotsToggled ->
                prefsRepository.preventScreenshots.set(event.preventScreenshots)

            // Handled in Compose
            SettingsUiEvent.NavigateBack -> Unit
            SettingsUiEvent.NavigateToDevTools -> Unit
            SettingsUiEvent.NavigateToBackupRestore -> Unit
        }
    }

}
