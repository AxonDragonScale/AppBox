package com.axondragonscale.limitless.ui.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.axondragonscale.commons.daysBetween
import com.axondragonscale.commons.today
import com.axondragonscale.limitless.database.entity.HabitEntity
import com.axondragonscale.limitless.database.view.HabitDayCountView
import com.axondragonscale.limitless.repository.LimitlessRepository
import com.axondragonscale.limitless.ui.model.MonthCount
import com.axondragonscale.limitless.ui.model.MonthlyTrendData
import com.axondragonscale.limitless.ui.model.WeeklyPatternData
import com.axondragonscale.limitless.ui.stats.model.GlobalHeatMapData
import com.axondragonscale.limitless.ui.stats.model.HabitConsistencyData
import com.axondragonscale.limitless.ui.stats.model.HabitConsistencyItem
import com.axondragonscale.limitless.ui.stats.model.TodayHabitStatus
import com.axondragonscale.limitless.ui.stats.model.TodayProgressData
import com.axondragonscale.limitless.ui.stats.model.WeekComparisonData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.minus
import kotlinx.datetime.number

/**
 * Created by Ronak Harkhani on 22/07/25
 */
class StatsVM(
    private val repository: LimitlessRepository,
) : ViewModel() {

    val uiState = MutableStateFlow(StatsUiState())

    init {
        viewModelScope.launch(Dispatchers.IO) {
            combine(
                repository.getHabits(),
                repository.getAllDayCounts(),
            ) { habits, dayCounts ->
                if (habits.isEmpty()) {
                    StatsUiState(isLoading = false, isEmpty = true)
                } else {
                    val today = LocalDate.today()
                    StatsUiState(
                        isLoading = false,
                        isEmpty = false,
                        todayProgress = computeTodayProgress(habits, dayCounts, today),
                        weekComparison = computeWeekComparison(dayCounts, today),
                        habitConsistency = computeHabitConsistency(habits, dayCounts, today),
                        heatMap = computeGlobalHeatMap(dayCounts, habits.size),
                        weeklyPattern = computeWeeklyPattern(dayCounts, today),
                        monthlyTrend = computeMonthlyTrend(dayCounts),
                    )
                }
            }.collect { state ->
                uiState.update { state }
            }
        }
    }

    fun onEvent(event: StatsUiEvent): Any = when (event) {
        StatsUiEvent.NavigateBack -> Unit  // Handled in Compose
    }

    // region Today's Progress

    private fun computeTodayProgress(
        habits: List<HabitEntity>,
        dayCounts: List<HabitDayCountView>,
        today: LocalDate,
    ): TodayProgressData {
        val recordedToday = dayCounts
            .filter { it.date == today }
            .map { it.habitId }
            .toSet()

        val statuses = habits.map { habit ->
            TodayHabitStatus(
                name = habit.name,
                color = habit.color,
                isCompleted = habit.id in recordedToday,
            )
        }

        return TodayProgressData(
            completed = recordedToday.size,
            total = habits.size,
            habits = statuses,
        )
    }

    // endregion

    // region Week Comparison

    private fun computeWeekComparison(
        dayCounts: List<HabitDayCountView>,
        today: LocalDate,
    ): WeekComparisonData {
        // ISO week starts Monday
        val thisWeekMonday = today.minus(today.dayOfWeek.ordinal, DateTimeUnit.DAY)
        val lastWeekMonday = thisWeekMonday.minus(1, DateTimeUnit.WEEK)

        val thisWeekDailyCounts = IntArray(7)
        val lastWeekDailyCounts = IntArray(7)

        for (dc in dayCounts) {
            when {
                dc.date >= thisWeekMonday && dc.date <= today ->
                    thisWeekDailyCounts[dc.date.dayOfWeek.ordinal] += dc.count

                dc.date >= lastWeekMonday && dc.date < thisWeekMonday ->
                    lastWeekDailyCounts[dc.date.dayOfWeek.ordinal] += dc.count
            }
        }

        val firstDate = dayCounts.minOfOrNull { it.date }
        val hasLastWeekData = firstDate != null && daysBetween(firstDate, today) >= 7

        return WeekComparisonData(
            thisWeekTotal = thisWeekDailyCounts.sum(),
            lastWeekTotal = lastWeekDailyCounts.sum(),
            thisWeekDailyCounts = thisWeekDailyCounts.toList(),
            lastWeekDailyCounts = lastWeekDailyCounts.toList(),
            hasLastWeekData = hasLastWeekData,
        )
    }

    // endregion

    // region Habit Consistency Ranking

    private fun computeHabitConsistency(
        habits: List<HabitEntity>,
        dayCounts: List<HabitDayCountView>,
        today: LocalDate,
    ): HabitConsistencyData {
        val dayCountsByHabit = dayCounts.groupBy { it.habitId }

        val items = habits.map { habit ->
            val habitDayCounts = dayCountsByHabit[habit.id].orEmpty()
            val activeDays = habitDayCounts.size
            val firstDate = habitDayCounts.minOfOrNull { it.date }
            val totalDays = if (firstDate != null) daysBetween(firstDate, today) + 1 else 1
            val consistencyRate = if (totalDays > 0) activeDays.toFloat() / totalDays else 0f

            HabitConsistencyItem(
                name = habit.name,
                color = habit.color,
                consistencyRate = consistencyRate,
                activeDays = activeDays,
                totalDays = totalDays,
            )
        }

        return HabitConsistencyData(
            habits = items.sortedByDescending { it.consistencyRate },
        )
    }

    // endregion

    // region Global Heat Map

    private fun computeGlobalHeatMap(
        dayCounts: List<HabitDayCountView>,
        totalHabits: Int,
    ): GlobalHeatMapData {
        // Each cell = number of distinct habits recorded on that date
        val habitsByDate = mutableMapOf<LocalDate, MutableSet<Long>>()
        for (dc in dayCounts) {
            habitsByDate.getOrPut(dc.date) { mutableSetOf() }.add(dc.habitId)
        }

        val dayCountsMap = habitsByDate.mapValues { (_, habitSet) -> habitSet.size }
        val maxCount = dayCountsMap.values.maxOrNull() ?: 0

        return GlobalHeatMapData(
            dayCounts = dayCountsMap,
            maxCount = maxCount,
            totalHabits = totalHabits,
        )
    }

    // endregion

    // region Weekly Pattern

    private fun computeWeeklyPattern(
        dayCounts: List<HabitDayCountView>,
        today: LocalDate,
    ): WeeklyPatternData {
        if (dayCounts.isEmpty()) return WeeklyPatternData(dayAverages = List(7) { 0f })

        // Sum total records per day-of-week index (0=Mon .. 6=Sun) across all habits
        val sumByDay = FloatArray(7)
        for (dc in dayCounts) {
            sumByDay[dc.date.dayOfWeek.isoDayNumber - 1] += dc.count.toFloat()
        }

        val firstDate = dayCounts.minOf { it.date }
        val completeWeeks = (daysBetween(firstDate, today) + 1) / 7

        val averages = List(7) { i ->
            if (completeWeeks > 0) sumByDay[i] / completeWeeks else 0f
        }

        return WeeklyPatternData(dayAverages = averages)
    }

    // endregion

    // region Monthly Trend

    private fun computeMonthlyTrend(
        dayCounts: List<HabitDayCountView>,
    ): MonthlyTrendData {
        // Group by year-month across all habits, sum counts, sort chronologically
        val monthMap = mutableMapOf<Pair<Int, Int>, Int>()
        for (dc in dayCounts) {
            val key = dc.date.year to dc.date.month.number
            monthMap[key] = (monthMap[key] ?: 0) + dc.count
        }

        val monthCounts = monthMap
            .map { (key, count) -> MonthCount(year = key.first, month = key.second, count = count) }
            .sortedWith(compareBy({ it.year }, { it.month }))

        return MonthlyTrendData(monthCounts = monthCounts)
    }

    // endregion
}
