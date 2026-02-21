package com.axondragonscale.limitless.ui.stats.model

/**
 * Created by Ronak Harkhani on 21/07/25
 *
 * Data for the Streak Cards widget.
 *
 * @param currentStreak Consecutive days with ≥1 record, up to and including today.
 * @param bestStreak Longest consecutive streak ever recorded.
 */
data class StreakData(
    val currentStreak: Int,
    val bestStreak: Int,
)
