package com.axondragonscale.wallmatic.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.axondragonscale.commons.collect
import com.axondragonscale.wallmatic.background.UpdateCoordinator
import com.axondragonscale.wallmatic.model.TargetScreen
import com.axondragonscale.wallmatic.model.copy
import com.axondragonscale.wallmatic.repository.AppPrefsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Created by Ronak Harkhani on 09/06/24
 */
internal class SettingsVM(
    private val appPrefsRepository: AppPrefsRepository,
    private val updateCoordinator: UpdateCoordinator,
) : ViewModel() {

    val uiState = MutableStateFlow(SettingsUiState())

    init {
        val mirrorHomeConfigForLockFlow = appPrefsRepository.configFlow
            .map { it.mirrorHomeConfigForLock }
            .distinctUntilChanged()

        viewModelScope.launch(Dispatchers.IO) {
            combine(
                appPrefsRepository.uiModeFlow,
                appPrefsRepository.dynamicThemeFlow,
                appPrefsRepository.gridSizeFlow,
                mirrorHomeConfigForLockFlow,
            ) { uiMode, dynamicTheme, gridSize, mirrorHomeConfigForLock ->
                uiState.update {
                    it.copy(
                        uiMode = uiMode,
                        dynamicTheme = dynamicTheme,
                        gridSize = gridSize,
                        mirrorHomeConfigForLock = mirrorHomeConfigForLock,
                    )
                }
            }.collect()
        }
    }

    fun onEvent(event: SettingsUiEvent) = viewModelScope.launch(Dispatchers.IO) {
        when (event) {
            is SettingsUiEvent.UIModeUpdated ->
                appPrefsRepository.setUiMode(event.uiMode)

            is SettingsUiEvent.DynamicThemeToggled ->
                appPrefsRepository.setDynamicTheme(event.dynamicTheme)

            is SettingsUiEvent.GridSizedUpdated ->
                appPrefsRepository.setGridSize(event.gridSize)

            is SettingsUiEvent.MirrorHomeConfigForLockToggled ->
                onMirrorHomeConfigForLockToggled(event.enabled)

            // Navigation events are handled in the composable
            is SettingsUiEvent.NavigateToDevTools -> Unit
        }
    }

    private suspend fun onMirrorHomeConfigForLockToggled(enabled: Boolean) {
        val config = appPrefsRepository.configFlow.firstOrNull() ?: return
        if (enabled && config.homeConfig.albumId <= 0) return

        appPrefsRepository.updateConfig { current ->
            if (enabled) {
                current.copy {
                    mirrorHomeConfigForLock = true
                    homeConfig = homeConfig.copy { autoCycleEnabled = true }
                }
            } else {
                current.copy { mirrorHomeConfigForLock = false }
            }
        }
        updateCoordinator.applyNow(if (enabled) TargetScreen.Home else TargetScreen.Lock)
    }

}
