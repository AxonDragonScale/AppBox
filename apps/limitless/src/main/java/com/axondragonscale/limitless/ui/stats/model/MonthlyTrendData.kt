package com.axondragonscale.limitless.ui.stats.model

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

/**
 * Total record count for a single calendar month.
 *
 * @param year Calendar year (e.g., 2025).
 * @param month Month number (1 = January .. 12 = December).
 * @param count Total records logged in this month.
 */
data class MonthCount(
    val year: Int,
    val month: Int,
    val count: Int,
)
