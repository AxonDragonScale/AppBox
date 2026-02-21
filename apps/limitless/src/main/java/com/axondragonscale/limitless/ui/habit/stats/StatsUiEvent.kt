package com.axondragonscale.limitless.ui.habit.stats

/**
 * Created by Ronak Harkhani on 03/11/24
 */
sealed interface StatsUiEvent {
    data class PeriodChanged(val period: HistoryPeriod) : StatsUiEvent
}

/**
 * Selectable time range for the Daily History chart.
 *
 * @param months Number of months to look back from today.
 * @param label Display label for the filter chip.
 */
enum class HistoryPeriod(val months: Int, val label: String) {
    ONE_MONTH(1, "1M"),
    THREE_MONTHS(3, "3M"),
    SIX_MONTHS(6, "6M"),
    ONE_YEAR(12, "1Y"),
}
