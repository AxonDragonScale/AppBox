package com.axondragonscale.limitless.ui.habit.calendar

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.axondragonscale.limitless.repository.LimitlessRepository
import com.axondragonscale.limitless.ui.Route
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.number
import kotlinx.datetime.plus
import kotlinx.datetime.todayIn
import kotlin.time.Clock

/**
 * Created by Ronak Harkhani on 29/06/25
 */
class HabitCalendarVM(
    savedStateHandle: SavedStateHandle,
    private val repository: LimitlessRepository,
) : ViewModel() {

    private val habitRoute = savedStateHandle.toRoute<Route.Habit>()
    private val habitId: Long = habitRoute.habitId

    val uiState = MutableStateFlow(
        run {
            val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
            HabitCalendarUiState(year = today.year, month = today.month.number)
        }
    )

    private var observeMonthJob: Job? = null

    init {
        observeHabit()
        observeCurrentMonth()
    }

    fun onEvent(event: HabitCalendarUiEvent): Any = when (event) {
        is HabitCalendarUiEvent.PreviousMonth -> shiftMonth(-1)
        is HabitCalendarUiEvent.NextMonth -> shiftMonth(1)
        is HabitCalendarUiEvent.AddRecord -> addRecord(event)

        // Handled in Compose
        is HabitCalendarUiEvent.NavigateToDayDetail -> Unit
    }

    private fun observeHabit() = viewModelScope.launch(Dispatchers.IO) {
        repository.getHabitFlow(habitId)
            .filterNotNull()
            .collect { habit ->
                uiState.update { it.copy(habitName = habit.name) }
            }
    }

    private fun shiftMonth(delta: Int) {
        val current = uiState.value
        val currentFirst = LocalDate(current.year, current.month, 1)
        val newFirst = if (delta > 0)
            currentFirst.plus(1, DateTimeUnit.MONTH)
        else
            currentFirst.minus(1, DateTimeUnit.MONTH)

        uiState.update {
            it.copy(year = newFirst.year, month = newFirst.monthNumber, isLoading = true)
        }
        observeCurrentMonth()
    }

    private fun observeCurrentMonth() {
        observeMonthJob?.cancel()
        observeMonthJob = viewModelScope.launch(Dispatchers.IO) {
            val state = uiState.value
            val firstOfMonth = LocalDate(state.year, state.month, 1)
            val lastOfMonth = firstOfMonth.plus(1, DateTimeUnit.MONTH).minus(1, DateTimeUnit.DAY)

            repository.getDayCountsForDateRange(habitRoute.habitId, firstOfMonth, lastOfMonth)
                .collect { dayCounts ->
                    val countsMap = dayCounts.associate { it.date to it.count }
                    uiState.update { it.copy(dayCounts = countsMap, isLoading = false) }
                }
        }
    }

    private fun addRecord(event: HabitCalendarUiEvent.AddRecord) = viewModelScope.launch(Dispatchers.IO) {
        repository.recordHabit(habitId, event.timestamp, event.message)
    }
}
