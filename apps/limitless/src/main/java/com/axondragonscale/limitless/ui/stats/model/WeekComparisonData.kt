package com.axondragonscale.limitless.ui.stats.model

/**
 * Created by Ronak Harkhani on 22/07/25
 *
 * Data for the "This Week vs Last Week" section.
 * Compares the total number of habit records logged this week against last week.
 *
 * @param thisWeekTotal Total records logged in the current ISO week (Mon–today).
 * @param lastWeekTotal Total records logged in the previous full ISO week (Mon–Sun).
 */
data class WeekComparisonData(
    val thisWeekTotal: Int,
    val lastWeekTotal: Int,
) {
    /** Signed difference: positive = improvement, negative = decline, zero = unchanged. */
    val delta: Int get() = thisWeekTotal - lastWeekTotal
}
