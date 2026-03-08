package com.axondragonscale.limitless.ui.habit.history

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

/**
 * Created by Ronak Harkhani on 29/06/25
 */
class HistoryVM(
    savedStateHandle: SavedStateHandle,
    private val repository: LimitlessRepository,
) : ViewModel() {

    companion object {
        private const val PAGE_SIZE = 20
    }

    private val habitRoute = savedStateHandle.toRoute<Route.Habit>()

    val uiState = MutableStateFlow(HistoryUiState())

    init {
        observeHabit()
        observeRecordCount()
    }

    fun onEvent(event: HistoryUiEvent): Any = when (event) {
        is HistoryUiEvent.LoadMore -> loadMore()
        is HistoryUiEvent.SaveRecord -> saveRecord(event)
        is HistoryUiEvent.DeleteRecord -> deleteRecord(event)
    }

    private fun observeHabit() = viewModelScope.launch(Dispatchers.Default) {
        repository.getHabitFlow(habitRoute.habitId)
            .filterNotNull()
            .collect { habit -> uiState.update { it.copy(habit = habit) } }
    }

    /**
     * Observe the record count so that any insert or delete triggers a full refresh.
     */
    private fun observeRecordCount() = viewModelScope.launch(Dispatchers.Default) {
        repository.getRecordCountFlow(habitRoute.habitId).collect {
            refresh()
        }
    }

    private fun saveRecord(event: HistoryUiEvent.SaveRecord) = viewModelScope.launch(Dispatchers.Default) {
        repository.saveRecord(event.record)
        refresh() // Since there is no change in count, manual refresh is required
    }

    private fun deleteRecord(event: HistoryUiEvent.DeleteRecord) = viewModelScope.launch(Dispatchers.Default) {
        repository.deleteRecord(event.id)
    }

    /**
     * Resets the list and loads the same count. Called whenever the DB count changes
     * (record added or deleted) so the list always reflects the current DB state and
     * the pagination offset is always consistent.
     */
    private suspend fun refresh() {
        val currentCount = uiState.value.records.size
        if (currentCount == 0) uiState.update { it.copy(isLoading = true) }

        val records = repository.getRecordsPaginated(habitRoute.habitId, currentCount, 0)
        val totalCount = repository.getRecordCount(habitRoute.habitId)
        uiState.update {
            it.copy(
                records = records,
                isLoading = false,
                hasMore = records.size < totalCount,
            )
        }
    }

    /**
     * Appends the next page to the current list. The offset is derived from the current
     * list size, which is always correct because [refresh] resets the list on every DB
     * count change before any LoadMore can be issued on the new data.
     */
    private fun loadMore() = viewModelScope.launch(Dispatchers.Default) {
        val currentState = uiState.value
        if (currentState.isLoading || currentState.isLoadingMore || !currentState.hasMore) return@launch

        uiState.update { it.copy(isLoadingMore = true) }

        val offset = uiState.value.records.size
        val records = repository.getRecordsPaginated(habitRoute.habitId, PAGE_SIZE, offset)
        val totalCount = repository.getRecordCount(habitRoute.habitId)

        uiState.update { state ->
            // A refresh may have fired while we were fetching — if so, discard the
            // stale page rather than appending it on top of the freshly loaded data.
            if (state.isLoading) return@update state
            state.copy(
                records = state.records + records,
                isLoadingMore = false,
                hasMore = state.records.size + records.size < totalCount,
            )
        }
    }
}
