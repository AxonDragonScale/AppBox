package com.axondragonscale.limitless.ui.habit

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.axondragonscale.limitless.repository.LimitlessRepository
import com.axondragonscale.limitless.ui.Route
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import kotlin.time.Clock

/**
 * Created by Ronak Harkhani on 17/02/26
 */
class HabitVM(
    savedStateHandle: SavedStateHandle,
    private val repository: LimitlessRepository,
) : ViewModel() {

    private val habitRoute = savedStateHandle.toRoute<Route.Habit>()

    val uiState = MutableStateFlow(HabitUiState())

    init {
        viewModelScope.launch(Dispatchers.IO) {
            val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
            combine(
                repository.getHabitFlow(habitRoute.habitId).filterNotNull(),
                repository.getDayCountsForDateRange(habitRoute.habitId, today, today),
            ) { habit, dayCounts ->
                val todayCount = dayCounts.firstOrNull()?.count ?: 0
                HabitUiState(
                    isLoading = false,
                    habit = habit,
                    todayCount = todayCount,
                )
            }.collect { state ->
                uiState.update { state }
            }
        }
    }

    fun onEvent(event: HabitUiEvent): Any = when (event) {
        is HabitUiEvent.RecordHabit -> recordHabit(event)

        // Handled in Compose
        HabitUiEvent.NavigateBack -> Unit
        HabitUiEvent.NavigateToEditor -> Unit
    }

    private fun recordHabit(event: HabitUiEvent.RecordHabit) = viewModelScope.launch(Dispatchers.IO) {
        repository.recordHabit(habitRoute.habitId, event.message)
    }

}
