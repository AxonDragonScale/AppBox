package com.axondragonscale.limitless.ui.editor

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.axondragonscale.limitless.database.entity.HabitEntity
import com.axondragonscale.limitless.repository.LimitlessRepository
import com.axondragonscale.limitless.ui.Route
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Created by Ronak Harkhani on 03/11/24
 */
class EditorVM(
    savedStateHandle: SavedStateHandle,
    private val repository: LimitlessRepository
) : ViewModel() {

    private val editorRoute = savedStateHandle.toRoute<Route.Editor>()

    val uiState = MutableStateFlow(EditorUiState())

    private val uiEffectChannel = Channel<EditorUiEffect>()
    val uiEffect = uiEffectChannel.receiveAsFlow().flowOn(Dispatchers.Main.immediate)

    init {
        viewModelScope.launch(Dispatchers.IO) {
            val habit = editorRoute.habitId?.let { repository.getHabit(it) } ?: HabitEntity.new()
            uiState.update { it.copy(isLoading = false, habit = habit) }
        }
    }

    fun onEvent(event: EditorUiEvent): Any = when (event) {
        is EditorUiEvent.UpdateHabit -> updateHabit(event)
        is EditorUiEvent.SaveHabit -> saveHabit(event)

        // Handled in compose
        EditorUiEvent.NavigateBack -> Unit
    }

    private fun updateHabit(event: EditorUiEvent.UpdateHabit) {
        uiState.update { it.copy(habit = event.habit) }
    }

    private fun saveHabit(
        event: EditorUiEvent.SaveHabit
    ) = viewModelScope.launch(Dispatchers.IO) {
        repository.saveHabit(habit = event.habit)
        uiEffectChannel.send(EditorUiEffect.NavigateBack)
    }

}
