package com.axondragonscale.limitless.ui.habit

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
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
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.TimeZone
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.minus
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
            val startOfWeek = today.minus(today.dayOfWeek.isoDayNumber - 1, DateTimeUnit.DAY)
            val startOfMonth = today.minus(today.day - 1, DateTimeUnit.DAY)
            val rangeStart = minOf(startOfWeek, startOfMonth)

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
        is HabitUiEvent.RecordHabit -> recordHabit(event)

        // Handled in Compose
        HabitUiEvent.NavigateBack -> Unit
        HabitUiEvent.NavigateToEditor -> Unit
    }

    private fun recordHabit(event: HabitUiEvent.RecordHabit) = viewModelScope.launch(Dispatchers.IO) {
        repository.recordHabit(habitRoute.habitId, event.message)
    }

}
