package com.axondragonscale.topty.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.axondragonscale.topty.repository.AppPrefsRepository
import com.axondragonscale.topty.util.DevTools
import com.axondragonscale.topty.util.collect
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Created by Ronak Harkhani on 13/10/24
 */
class SettingsVM(
    private val appPrefsRepository: AppPrefsRepository,
    private val devTools: DevTools,
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
            ) { uiMode, dynamicTheme, uiFont, codeDesign, speakOutOnTap ->
                uiState.update {
                    it.copy(
                        uiMode = uiMode,
                        dynamicTheme = dynamicTheme,
                        uiFont = uiFont,
                        codeDesign = codeDesign,
                        speakOutOnTap = speakOutOnTap,
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

            is SettingsUiEvent.ClearData ->
                devTools.clearData()

            is SettingsUiEvent.CodeDesignUpdated ->
                appPrefsRepository.codeDesign.set(event.codeDesign)

            is SettingsUiEvent.SpeakOutOnTapToggled ->
                appPrefsRepository.speakOutOnTap.set(event.speakOutOnTap)

            // Handled in Compose
            SettingsUiEvent.NavigateBack -> Unit
        }
    }

}
