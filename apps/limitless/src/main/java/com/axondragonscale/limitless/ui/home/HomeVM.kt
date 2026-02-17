package com.axondragonscale.limitless.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.axondragonscale.limitless.repository.AppPrefsRepository
import com.axondragonscale.limitless.repository.LimitlessRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Created by Ronak Harkhani on 01/11/24
 */
class HomeVM(
    private val repository: LimitlessRepository,
    private val appPrefsRepository: AppPrefsRepository,
) : ViewModel() {

    val uiState = MutableStateFlow(HomeUiState())

    init {
        viewModelScope.launch(Dispatchers.IO) {
            combine(
                repository.getHabitHeatMapData(),
                appPrefsRepository.highlightCurrentDay.flow,
            ) { heatMapData, highlightCurrentDay ->
                HomeUiState(
                    isLoading = false,
                    habits = heatMapData,
                    highlightCurrentDay = highlightCurrentDay,
                )
            }.collect { state ->
                uiState.update { state }
            }
        }
    }

    fun onEvent(event: HomeUiEvent) = when (event) {
        is HomeUiEvent.RecordHabit -> recordHabit(event)

        // Handled in Compose
        is HomeUiEvent.NavigateToSettings -> Unit
        is HomeUiEvent.NavigateToStats -> Unit
        is HomeUiEvent.AddHabit -> Unit
    }

    private fun recordHabit(event: HomeUiEvent.RecordHabit) =
        viewModelScope.launch(Dispatchers.IO) {
            repository.recordHabit(event.habitId, event.message)
        }

}
