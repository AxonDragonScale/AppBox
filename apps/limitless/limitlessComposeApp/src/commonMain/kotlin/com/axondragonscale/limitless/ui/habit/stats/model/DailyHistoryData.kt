package com.axondragonscale.limitless.ui.habit.stats.model

import kotlinx.datetime.LocalDate

/**
 * Created by Ronak Harkhani on 21/07/25
 *
 * Data for the Daily History chart widget.
 * Contains up to 1 year of daily record counts.
 * Period filtering (1M/3M/6M/1Y) is handled in the UI layer using this full dataset.
 *
 * @param dayCounts Map of date to record count for each active day.
 */
data class DailyHistoryData(
    val dayCounts: Map<LocalDate, Int>,
)
