package com.axondragonscale.limitless.ui.habit.stats

import com.axondragonscale.limitless.model.HabitColor
import com.axondragonscale.limitless.ui.habit.stats.model.DailyHistoryData
import com.axondragonscale.limitless.ui.habit.stats.model.GoalCompletionData
import com.axondragonscale.limitless.ui.model.MonthlyTrendData
import com.axondragonscale.limitless.ui.habit.stats.model.StreakData
import com.axondragonscale.limitless.ui.habit.stats.model.SummaryData
import com.axondragonscale.limitless.ui.habit.stats.model.TimeOfDayData
import com.axondragonscale.limitless.ui.model.WeeklyPatternData

/**
 * Created by Ronak Harkhani on 03/11/24
 */
data class HabitStatsUiState(
    val isLoading: Boolean = true,
    val habitColor: HabitColor = HabitColor.PURPLE,
    val streakData: StreakData? = null,
    val dailyHistoryData: DailyHistoryData? = null,
    val timeOfDayData: TimeOfDayData? = null,
    val weeklyPatternData: WeeklyPatternData? = null,
    val monthlyTrendData: MonthlyTrendData? = null,
    val goalCompletionData: GoalCompletionData? = null,
    val summaryData: SummaryData? = null,
)
