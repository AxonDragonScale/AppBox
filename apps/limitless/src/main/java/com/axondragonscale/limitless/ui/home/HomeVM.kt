package com.axondragonscale.limitless.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.axondragonscale.limitless.model.HabitHeatMapData
import com.axondragonscale.limitless.repository.AppPrefsRepository
import com.axondragonscale.limitless.repository.LimitlessRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

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
                repository.getHabitsWithRecords(),
                appPrefsRepository.highlightCurrentDay.flow,
            ) { (habits, records), highlightCurrentDay ->
                val recordsByHabit = records.groupBy { it.habitId }
                val timeZone = TimeZone.currentSystemDefault()

                val heatMapDataList = habits.map { habit ->
                    val habitRecords = recordsByHabit[habit.id].orEmpty()
                    val dayCounts = habitRecords
                        .groupBy { record ->
                            Instant.fromEpochMilliseconds(record.timestamp)
                                .toLocalDateTime(timeZone)
                                .date
                        }
                        .mapValues { (_, records) -> records.size }

                    val maxCount = dayCounts.values.maxOrNull() ?: 0

                    HabitHeatMapData(
                        habitId = habit.id,
                        habitName = habit.name,
                        dayCounts = dayCounts,
                        maxCount = maxCount,
                    )
                }

                HomeUiState(
                    isLoading = false,
                    habits = heatMapDataList,
                    highlightCurrentDay = highlightCurrentDay,
                )
            }.collect { state ->
                uiState.update { state }
            }
        }
    }

    fun onEvent(event: HomeUiEvent) = when (event) {

        // Handled in Compose
        HomeUiEvent.NavigateToSettings -> Unit
        HomeUiEvent.NavigateToStats -> Unit
        HomeUiEvent.AddHabit -> Unit
    }

}
