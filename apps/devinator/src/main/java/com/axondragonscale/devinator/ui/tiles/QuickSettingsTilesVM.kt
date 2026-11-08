package com.axondragonscale.devinator.ui.tiles

import android.content.ComponentName
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.axondragonscale.devinator.model.DurationOption
import com.axondragonscale.devinator.repository.QuickSettingsTileRepository
import com.axondragonscale.devinator.repository.TimedToggleRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Created by Ronak on 11/06/26 */
class QuickSettingsTilesVM(
    private val repository: QuickSettingsTileRepository,
    private val timedToggle: TimedToggleRepository,
) : ViewModel() {

    val uiState: StateFlow<QuickSettingsTilesUiState> = combine(
        repository.addedClassNames,
        timedToggle.duration,
    ) { added, duration ->
        QuickSettingsTilesUiState(
            addedClassNames = added,
            timedToggleDuration = duration,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = QuickSettingsTilesUiState(
            addedClassNames = repository.currentClassNames(),
            timedToggleDuration = timedToggle.duration.value,
        ),
    )

    private val effectChannel = Channel<QuickSettingsTilesUiEffect>()
    val effect = effectChannel.receiveAsFlow().flowOn(Dispatchers.Main.immediate)

    fun onEvent(event: QuickSettingsTilesUiEvent): Any = when (event) {
        is QuickSettingsTilesUiEvent.Remove -> remove(event.component)
    }

    private fun remove(component: ComponentName) = viewModelScope.launch {
        if (!repository.remove(component)) {
            effectChannel.send(QuickSettingsTilesUiEffect.ShowSnackbar("Couldn't remove this tile."))
        }
    }
}

data class QuickSettingsTilesUiState(
    val addedClassNames: Set<String> = emptySet(),
    val timedToggleDuration: DurationOption = DurationOption.MIN_5,
)

sealed interface QuickSettingsTilesUiEvent {
    data class Remove(val component: ComponentName) : QuickSettingsTilesUiEvent
}

sealed interface QuickSettingsTilesUiEffect {
    data class ShowSnackbar(val message: String) : QuickSettingsTilesUiEffect
}
