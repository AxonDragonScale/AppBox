package com.axondragonscale.limitless.ui.habit

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.axondragonscale.commons.startOfMonth
import com.axondragonscale.commons.startOfWeek
import com.axondragonscale.commons.today
import com.axondragonscale.limitless.model.GoalStatus
import com.axondragonscale.limitless.model.GoalType
import com.axondragonscale.limitless.repository.LimitlessRepository
import com.axondragonscale.limitless.ui.Route
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate

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
            val today = LocalDate.today()
            val startOfWeek = today.startOfWeek
            val rangeStart = minOf(startOfWeek, today.startOfMonth)

            combine(
                repository.getHabitFlow(habitRoute.habitId).filterNotNull(),
                repository.getGoalsForHabit(habitRoute.habitId),
                repository.getDayCountsForDateRange(habitRoute.habitId, rangeStart, today),
            ) { habit, goals, dayCounts ->
                val todayCount = dayCounts.firstOrNull { it.date == today }?.count ?: 0

                val goalStatuses = goals
                    .sortedBy { it.type.ordinal }
                    .map { goal ->
                        val current = when (goal.type) {
                            GoalType.DAILY -> todayCount
                            GoalType.WEEKLY -> dayCounts.filter { it.date >= startOfWeek }.sumOf { it.count }
                            GoalType.MONTHLY -> dayCounts.sumOf { it.count }
                        }
                        GoalStatus(goal, current)
                    }

                HabitUiState(
                    isLoading = false,
                    habit = habit,
                    todayCount = todayCount,
                    goals = goalStatuses,
                )
            }.collect { state ->
                uiState.update { state }
            }
        }
    }

    fun onEvent(event: HabitUiEvent): Any = when (event) {
        is HabitUiEvent.SaveRecord -> saveRecord(event)

        // Handled in Compose
        is HabitUiEvent.NavigateBack -> Unit
        is HabitUiEvent.NavigateToEditor -> Unit
        is HabitUiEvent.NavigateToDayDetail -> Unit
    }

    private fun saveRecord(event: HabitUiEvent.SaveRecord) = viewModelScope.launch(Dispatchers.IO) {
        repository.saveRecord(event.record)
    }

}
