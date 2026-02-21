package com.axondragonscale.limitless.ui.stats

import com.axondragonscale.limitless.model.HabitColor
import com.axondragonscale.limitless.ui.stats.model.DailyHistoryData
import com.axondragonscale.limitless.ui.stats.model.GoalCompletionData
import com.axondragonscale.limitless.ui.stats.model.MonthlyTrendData
import com.axondragonscale.limitless.ui.stats.model.StreakData
import com.axondragonscale.limitless.ui.stats.model.SummaryData
import com.axondragonscale.limitless.ui.stats.model.TimeOfDayData
import com.axondragonscale.limitless.ui.stats.model.WeeklyPatternData

/**
 * Created by Ronak Harkhani on 03/11/24
 */
data class StatsUiState(
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
