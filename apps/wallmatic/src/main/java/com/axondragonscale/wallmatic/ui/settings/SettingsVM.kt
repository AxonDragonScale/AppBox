package com.axondragonscale.wallmatic.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.axondragonscale.wallmatic.background.UpdateCoordinator
import com.axondragonscale.wallmatic.model.TargetScreen
import com.axondragonscale.wallmatic.repository.ConfigRepository
import com.axondragonscale.wallmatic.repository.PreferencesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

/**
 * Created by Ronak Harkhani on 09/06/24
 */
internal class SettingsVM(
    private val preferencesRepository: PreferencesRepository,
    private val configRepository: ConfigRepository,
    private val updateCoordinator: UpdateCoordinator,
) : ViewModel() {

    val uiState = MutableStateFlow(SettingsUiState())

    init {
        val mirrorHomeForLockFlow = configRepository.configFlow
            .map { it.mirrorHomeForLock }
            .distinctUntilChanged()

        viewModelScope.launch(Dispatchers.IO) {
            combine(
                preferencesRepository.uiMode.flow,
                preferencesRepository.dynamicTheme.flow,
                preferencesRepository.gridSize.flow,
                mirrorHomeForLockFlow,
            ) { uiMode, dynamicTheme, gridSize, mirrorHomeForLock ->
                SettingsUiState(
                    uiMode = uiMode,
                    dynamicTheme = dynamicTheme,
                    gridSize = gridSize,
                    mirrorHomeForLock = mirrorHomeForLock,
                )
            }.collect { uiState.value = it }
        }
    }

    fun onEvent(event: SettingsUiEvent) = viewModelScope.launch(Dispatchers.IO) {
        when (event) {
            is SettingsUiEvent.UIModeUpdated ->
                preferencesRepository.uiMode.set(event.uiMode)

            is SettingsUiEvent.DynamicThemeToggled ->
                preferencesRepository.dynamicTheme.set(event.dynamicTheme)

            is SettingsUiEvent.GridSizedUpdated ->
                preferencesRepository.gridSize.set(event.gridSize)

            is SettingsUiEvent.MirrorHomeForLockToggled ->
                onMirrorHomeForLockToggled(event.enabled)

            // Navigation events are handled in the composable
            is SettingsUiEvent.NavigateToDevTools -> Unit
        }
    }

    private suspend fun onMirrorHomeForLockToggled(enabled: Boolean) {
        val config = configRepository.configFlow.firstOrNull() ?: return
        if (enabled && config.home.albumId == null) return
        configRepository.update { it.copy(mirrorHomeForLock = enabled) }
        updateCoordinator.applyNow(if (enabled) TargetScreen.Home else TargetScreen.Lock)
    }

}
