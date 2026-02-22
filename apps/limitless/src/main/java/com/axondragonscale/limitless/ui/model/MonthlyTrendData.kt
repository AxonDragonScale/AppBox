package com.axondragonscale.limitless.ui.model

/**
 * Created by Ronak Harkhani on 21/07/25
 *
 * Data for the Monthly Trend bar chart widget.
 * Shows total records per month to visualize growth or decline over time.
 *
 * @param monthCounts Per-month record totals, ordered chronologically.
 */
data class MonthlyTrendData(
    val monthCounts: List<MonthCount>,
)
