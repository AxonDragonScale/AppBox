package com.axondragonscale.devinator.ui.tiles

import android.content.ComponentName
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.axondragonscale.devinator.repository.QuickSettingsTileRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Created by Ronak on 11/06/26 */
class QuickSettingsTilesVM(
    private val repository: QuickSettingsTileRepository,
) : ViewModel() {

    val uiState: StateFlow<QuickSettingsTilesUiState> = repository.addedClassNames
        .map { QuickSettingsTilesUiState(addedClassNames = it) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = QuickSettingsTilesUiState(repository.currentClassNames()),
        )

    private val effectChannel = Channel<QuickSettingsTilesUiEffect>()
    val effect = effectChannel.receiveAsFlow().flowOn(Dispatchers.Main.immediate)

    fun onEvent(event: QuickSettingsTilesUiEvent): Any = when (event) {
        is QuickSettingsTilesUiEvent.Add -> add(event.component)
        is QuickSettingsTilesUiEvent.Remove -> remove(event.component)
    }

    private fun add(component: ComponentName) = viewModelScope.launch {
        if (!repository.add(component)) {
            effectChannel.send(QuickSettingsTilesUiEffect.ShowSnackbar("Couldn't add this tile."))
        }
    }

    private fun remove(component: ComponentName) = viewModelScope.launch {
        if (!repository.remove(component)) {
            effectChannel.send(QuickSettingsTilesUiEffect.ShowSnackbar("Couldn't remove this tile."))
        }
    }
}

data class QuickSettingsTilesUiState(
    val addedClassNames: Set<String> = emptySet(),
)

sealed interface QuickSettingsTilesUiEvent {
    data class Add(val component: ComponentName) : QuickSettingsTilesUiEvent

    data class Remove(val component: ComponentName) : QuickSettingsTilesUiEvent
}

sealed interface QuickSettingsTilesUiEffect {
    data class ShowSnackbar(val message: String) : QuickSettingsTilesUiEffect
}
