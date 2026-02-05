package com.axondragonscale.topty.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.axondragonscale.commons.collect
import com.axondragonscale.commons.combine
import com.axondragonscale.topty.repository.AppPrefsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Created by Ronak Harkhani on 13/10/24
 */
class SettingsVM(
    private val appPrefsRepository: AppPrefsRepository,
): ViewModel() {

    val uiState = MutableStateFlow(SettingsUiState())

    init {
        viewModelScope.launch(Dispatchers.IO) {
            combine(
                appPrefsRepository.uiMode.flow,
                appPrefsRepository.dynamicTheme.flow,
                appPrefsRepository.uiFont.flow,
                appPrefsRepository.codeDesign.flow,
                appPrefsRepository.speakOutOnTap.flow,
                appPrefsRepository.biometricLock.flow,
            ) { uiMode, dynamicTheme, uiFont, codeDesign, speakOutOnTap, biometricLock ->
                uiState.update {
                    it.copy(
                        uiMode = uiMode,
                        dynamicTheme = dynamicTheme,
                        uiFont = uiFont,
                        codeDesign = codeDesign,
                        speakOutOnTap = speakOutOnTap,
                        biometricLock = biometricLock,
                    )
                }
            }.collect()
        }
    }

    fun onEvent(event: SettingsUiEvent) = viewModelScope.launch(Dispatchers.IO) {
        when (event) {
            is SettingsUiEvent.UIModeUpdated ->
                appPrefsRepository.uiMode.set(event.uiMode)

            is SettingsUiEvent.DynamicThemeToggled ->
                appPrefsRepository.dynamicTheme.set(event.dynamicTheme)

            is SettingsUiEvent.UIFontUpdated ->
                appPrefsRepository.uiFont.set(event.uiFont)

            is SettingsUiEvent.CodeDesignUpdated ->
                appPrefsRepository.codeDesign.set(event.codeDesign)

            is SettingsUiEvent.SpeakOutOnTapToggled ->
                appPrefsRepository.speakOutOnTap.set(event.speakOutOnTap)

            is SettingsUiEvent.BiometricLockToggled ->
                appPrefsRepository.biometricLock.set(event.biometricLock)

            // Handled in Compose
            SettingsUiEvent.NavigateBack -> Unit
            SettingsUiEvent.NavigateToDevTools -> Unit
        }
    }

}
