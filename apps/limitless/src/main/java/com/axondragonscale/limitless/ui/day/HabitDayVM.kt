package com.axondragonscale.limitless.ui.day

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.axondragonscale.limitless.repository.LimitlessRepository
import com.axondragonscale.limitless.ui.Route
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate

/**
 * Created by Ronak Harkhani on 29/06/25
 */
class HabitDayVM(
    savedStateHandle: SavedStateHandle,
    private val repository: LimitlessRepository,
) : ViewModel() {

    private val route = savedStateHandle.toRoute<Route.HabitDay>()
    private val habitId = route.habitId
    private val date = LocalDate.parse(route.date)

    val uiState = MutableStateFlow(HabitDayUiState(date = date))

    init {
        observeHabit()
        observeRecords()
    }

    fun onEvent(event: HabitDayUiEvent): Any = when (event) {
        is HabitDayUiEvent.SaveRecord -> saveRecord(event)
        is HabitDayUiEvent.DeleteRecord -> deleteRecord(event)

        // Handled in Compose
        HabitDayUiEvent.NavigateBack -> Unit
    }

    private fun observeHabit() = viewModelScope.launch(Dispatchers.IO) {
        repository.getHabitFlow(habitId)
            .filterNotNull()
            .collect { habit ->
                uiState.update { it.copy(habit = habit) }
            }
    }

    private fun observeRecords() = viewModelScope.launch(Dispatchers.IO) {
        repository.getRecordsForDate(habitId, date)
            .collect { records ->
                uiState.update { it.copy(records = records, isLoading = false) }
            }
    }

    private fun saveRecord(event: HabitDayUiEvent.SaveRecord) = viewModelScope.launch(Dispatchers.IO) {
        repository.saveRecord(event.record)
    }

    private fun deleteRecord(event: HabitDayUiEvent.DeleteRecord) = viewModelScope.launch(Dispatchers.IO) {
        repository.deleteRecord(event.id)
    }
}
