package com.axondragonscale.limitless.ui.stats

import com.axondragonscale.limitless.ui.model.MonthlyTrendData
import com.axondragonscale.limitless.ui.model.WeeklyPatternData
import com.axondragonscale.limitless.ui.stats.model.GlobalHeatMapData
import com.axondragonscale.limitless.ui.stats.model.HabitConsistencyData
import com.axondragonscale.limitless.ui.stats.model.TodayProgressData
import com.axondragonscale.limitless.ui.stats.model.WeekComparisonData

/**
 * Created by Ronak Harkhani on 23/07/25
 */
data class StatsUiState(
    val isLoading: Boolean = true,
    val isEmpty: Boolean = false,               // true when no habits exist
    val todayProgress: TodayProgressData? = null,
    val weekComparison: WeekComparisonData? = null,
    val habitConsistency: HabitConsistencyData? = null,
    val heatMap: GlobalHeatMapData? = null,
    val weeklyPattern: WeeklyPatternData? = null,
    val monthlyTrend: MonthlyTrendData? = null,
)
