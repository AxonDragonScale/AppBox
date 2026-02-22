package com.axondragonscale.limitless.ui.habit.stats

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.axondragonscale.limitless.database.entity.GoalEntity
import com.axondragonscale.limitless.database.view.HabitDayCountView
import com.axondragonscale.limitless.database.view.HabitHourCountView
import com.axondragonscale.limitless.model.GoalType
import com.axondragonscale.limitless.repository.LimitlessRepository
import com.axondragonscale.limitless.ui.Route
import com.axondragonscale.limitless.ui.habit.stats.model.DailyHistoryData
import com.axondragonscale.limitless.ui.habit.stats.model.GoalCompletionData
import com.axondragonscale.limitless.ui.habit.stats.model.GoalCompletionRate
import com.axondragonscale.limitless.ui.model.MonthCount
import com.axondragonscale.limitless.ui.model.MonthlyTrendData
import com.axondragonscale.limitless.ui.habit.stats.model.StreakData
import com.axondragonscale.limitless.ui.habit.stats.model.SummaryData
import com.axondragonscale.limitless.ui.habit.stats.model.TimeOfDayData
import com.axondragonscale.limitless.ui.model.WeeklyPatternData
import com.axondragonscale.commons.daysBetween
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.number
import kotlinx.datetime.todayIn
import kotlin.time.Clock

/**
 * Created by Ronak Harkhani on 03/11/24
 */
class HabitStatsVM(
    savedStateHandle: SavedStateHandle,
    private val repository: LimitlessRepository,
) : ViewModel() {

    private val habitRoute = savedStateHandle.toRoute<Route.Habit>()
    private val habitId: Long = habitRoute.habitId

    val uiState = MutableStateFlow(HabitStatsUiState())

    init {
        viewModelScope.launch(Dispatchers.IO) {
            combine(
                repository.getHabitFlow(habitId).filterNotNull(),
                repository.getDayCountsForHabit(habitId),
                repository.getHourCountsForHabit(habitId),
                repository.getGoalsForHabit(habitId),
            ) { habit, dayCounts, hourCounts, goals ->
                val today = Clock.System.todayIn(TimeZone.currentSystemDefault())

                HabitStatsUiState(
                    isLoading = false,
                    habitColor = habit.color,
                    streakData = computeStreaks(dayCounts, today),
                    dailyHistoryData = computeDailyHistory(dayCounts),
                    timeOfDayData = computeTimeOfDay(hourCounts),
                    weeklyPatternData = computeWeeklyPattern(dayCounts, today),
                    monthlyTrendData = computeMonthlyTrend(dayCounts),
                    goalCompletionData = computeGoalCompletion(goals, dayCounts, today),
                    summaryData = computeSummary(dayCounts),
                )
            }.collect { state ->
                uiState.update { state }
            }
        }
    }

    fun onEvent(event: HabitStatsUiEvent): Any = when (event) {
        // Period filtering is handled locally in the DailyHistoryChart composable.
        // This event is here for future use (e.g., persisting the selection).
        is HabitStatsUiEvent.PeriodChanged -> Unit
    }

    // region Streak Computation

    private fun computeStreaks(dayCounts: List<HabitDayCountView>, today: LocalDate): StreakData {
        if (dayCounts.isEmpty()) return StreakData(currentStreak = 0, bestStreak = 0)

        val activeDates = dayCounts.map { it.date }.toSortedSet()

        // Current streak: walk backwards from today
        var currentStreak = 0
        var date = today
        while (date in activeDates) {
            currentStreak++
            date = date.minus(1, DateTimeUnit.DAY)
        }

        // Best streak: scan all dates chronologically
        var bestStreak = 0
        var runningStreak = 0
        var previousDate: LocalDate? = null
        for (d in activeDates) {
            runningStreak = if (previousDate != null && d == previousDate.plus(1, DateTimeUnit.DAY)) {
                runningStreak + 1
            } else {
                1
            }
            if (runningStreak > bestStreak) bestStreak = runningStreak
            previousDate = d
        }

        return StreakData(currentStreak = currentStreak, bestStreak = bestStreak)
    }

    // endregion

    // region Daily History

    private fun computeDailyHistory(dayCounts: List<HabitDayCountView>): DailyHistoryData {
        val map = dayCounts.associate { it.date to it.count }
        return DailyHistoryData(dayCounts = map)
    }

    // endregion

    // region Time of Day

    private fun computeTimeOfDay(hourCounts: List<HabitHourCountView>): TimeOfDayData {
        val counts = MutableList(24) { 0 }
        for (hc in hourCounts) {
            if (hc.hour in 0..23) counts[hc.hour] = hc.count
        }
        val peakHour = if (counts.all { it == 0 }) -1 else counts.indices.maxBy { counts[it] }
        return TimeOfDayData(hourCounts = counts, peakHour = peakHour)
    }

    // endregion

    // region Weekly Pattern

    private fun computeWeeklyPattern(dayCounts: List<HabitDayCountView>, today: LocalDate): WeeklyPatternData {
        if (dayCounts.isEmpty()) return WeeklyPatternData(dayAverages = List(7) { 0f })

        // Sum total records per day-of-week index (0=Mon .. 6=Sun).
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

    private fun computeMonthlyTrend(dayCounts: List<HabitDayCountView>): MonthlyTrendData {
        // Group by year-month, sum counts, sort chronologically.
        val monthMap = mutableMapOf<Pair<Int, Int>, Int>()  // (year, month) -> total count
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

    // region Goal Completion

    private fun computeGoalCompletion(
        goals: List<GoalEntity>,
        dayCounts: List<HabitDayCountView>,
        today: LocalDate,
    ): GoalCompletionData? {
        if (goals.isEmpty()) return null
        if (dayCounts.isEmpty()) {
            return GoalCompletionData(
                rates = goals
                    .sortedBy { it.type.ordinal }
                    .map { GoalCompletionRate(type = it.type, target = it.target, percentage = 0f) }
            )
        }

        val firstDate = dayCounts.minOf { it.date }

        val rates = goals
            .sortedBy { it.type.ordinal }
            .map { goal ->
                val percentage = when (goal.type) {
                    GoalType.DAILY -> computeDailyGoalRate(dayCounts, goal.target, firstDate, today)
                    GoalType.WEEKLY -> computeWeeklyGoalRate(dayCounts, goal.target, firstDate, today)
                    GoalType.MONTHLY -> computeMonthlyGoalRate(dayCounts, goal.target, firstDate, today)
                }
                GoalCompletionRate(type = goal.type, target = goal.target, percentage = percentage)
            }

        return GoalCompletionData(rates = rates)
    }

    private fun computeDailyGoalRate(
        dayCounts: List<HabitDayCountView>,
        target: Int,
        firstDate: LocalDate,
        today: LocalDate,
    ): Float {
        val metDays = dayCounts.count { it.count >= target }
        val totalDays = daysBetween(firstDate, today) + 1
        return if (totalDays > 0) metDays.toFloat() / totalDays else 0f
    }

    private fun computeWeeklyGoalRate(
        dayCounts: List<HabitDayCountView>,
        target: Int,
        firstDate: LocalDate,
        today: LocalDate,
    ): Float {
        // Group by ISO week (week starts Monday)
        val weekSums = mutableMapOf<Pair<Int, Int>, Int>()  // (year, weekOfYear) -> total
        for (dc in dayCounts) {
            val key = isoWeekKey(dc.date)
            weekSums[key] = (weekSums[key] ?: 0) + dc.count
        }
        val metWeeks = weekSums.values.count { it >= target }

        // Total weeks from first date to today
        val totalWeeks = (daysBetween(firstDate, today) / 7) + 1
        return if (totalWeeks > 0) metWeeks.toFloat() / totalWeeks else 0f
    }

    private fun computeMonthlyGoalRate(
        dayCounts: List<HabitDayCountView>,
        target: Int,
        firstDate: LocalDate,
        today: LocalDate,
    ): Float {
        val monthSums = mutableMapOf<Pair<Int, Int>, Int>()  // (year, month) -> total
        for (dc in dayCounts) {
            val key = dc.date.year to dc.date.month.number
            monthSums[key] = (monthSums[key] ?: 0) + dc.count
        }
        val metMonths = monthSums.values.count { it >= target }

        // Total months from first date to today
        val totalMonths = (today.year - firstDate.year) * 12 + (today.month.number - firstDate.month.number) + 1
        return if (totalMonths > 0) metMonths.toFloat() / totalMonths else 0f
    }

    // endregion

    // region Summary

    private fun computeSummary(dayCounts: List<HabitDayCountView>): SummaryData {
        val totalRecords = dayCounts.sumOf { it.count }
        val activeDays = dayCounts.size
        val avgPerActiveDay = if (activeDays > 0) totalRecords.toFloat() / activeDays else 0f
        val trackingSince = dayCounts.minOfOrNull { it.date }

        return SummaryData(
            totalRecords = totalRecords,
            activeDays = activeDays,
            avgPerActiveDay = avgPerActiveDay,
            trackingSince = trackingSince,
        )
    }

    // endregion

    // region Helpers

    /**
     * Returns a key representing the ISO week for grouping.
     * Uses a simple approach: the Monday of the week as (year, dayOfYear).
     */
    private fun isoWeekKey(date: LocalDate): Pair<Int, Int> {
        val daysSinceMonday = date.dayOfWeek.isoDayNumber - 1
        val monday = date.minus(daysSinceMonday, DateTimeUnit.DAY)
        return monday.year to monday.dayOfYear
    }

    // endregion
}
