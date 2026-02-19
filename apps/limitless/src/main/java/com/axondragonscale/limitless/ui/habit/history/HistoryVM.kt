package com.axondragonscale.limitless.ui.habit.history

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.axondragonscale.limitless.repository.LimitlessRepository
import com.axondragonscale.limitless.ui.Route
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
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
        loadPage()
    }

    fun onEvent(event: HistoryUiEvent): Any = when (event) {
        is HistoryUiEvent.LoadMore -> loadPage()
    }

    private fun loadPage() = viewModelScope.launch(Dispatchers.IO) {
        if (uiState.value.isLoadingMore) return@launch
        uiState.update { it.copy(isLoadingMore = true) }

        val offset = uiState.value.records.size
        val records = repository.getRecordsPaginated(habitRoute.habitId, PAGE_SIZE, offset)
        val totalCount = repository.getRecordCount(habitRoute.habitId)

        uiState.update {
            it.copy(
                records = it.records + records,
                isLoading = false,
                isLoadingMore = false,
                hasMore = offset + records.size < totalCount,
            )
        }
    }
}
