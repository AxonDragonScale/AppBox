package com.axondragonscale.limitless.ui.stats.model

/**
 * Created by Ronak Harkhani on 22/07/25
 *
 * Data for the "This Week vs Last Week" section.
 * Compares the total number of habit records logged this week against last week.
 *
 * @param thisWeekTotal Total records logged in the current ISO week (Mon–today).
 * @param lastWeekTotal Total records logged in the previous full ISO week (Mon–Sun).
 * @param thisWeekDailyCounts Per-day record totals for this week (Mon=0 .. Sun=6), summed
 *   across all habits. Used to render the 7-day mini bar chart.
 * @param lastWeekDailyCounts Per-day record totals for last week (Mon=0 .. Sun=6), summed
 *   across all habits. Used to render the 7-day mini bar chart.
 * @param hasLastWeekData True when the user has at least 7 days of history, enabling the
 *   side-by-side comparison. False during the first week — only "This Week" is shown.
 */
data class WeekComparisonData(
    val thisWeekTotal: Int,
    val lastWeekTotal: Int,
    val thisWeekDailyCounts: List<Int>,  // 7 entries, Mon=0 .. Sun=6
    val lastWeekDailyCounts: List<Int>,  // 7 entries, Mon=0 .. Sun=6
    val hasLastWeekData: Boolean,
) {
    /** Signed difference: positive = improvement, negative = decline, zero = unchanged. */
    val delta: Int get() = thisWeekTotal - lastWeekTotal
}
