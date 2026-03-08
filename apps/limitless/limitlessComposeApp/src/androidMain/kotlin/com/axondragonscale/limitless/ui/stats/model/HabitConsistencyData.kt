package com.axondragonscale.limitless.ui.stats.model

import com.axondragonscale.limitless.model.HabitColor

/**
 * Created by Ronak Harkhani on 22/07/25
 *
 * Data for the Habit Consistency Ranking section.
 * Habits are sorted by consistency rate descending so the most consistent habit is first.
 */
data class HabitConsistencyData(
    val habits: List<HabitConsistencyItem>,
)

/**
 * Per-habit consistency summary for the ranking list.
 *
 * @param name            Habit display name.
 * @param color           Habit color, used to tint the bar and icon.
 * @param consistencyRate Fraction of [totalDays] on which the habit was recorded (0f..1f).
 * @param activeDays      Number of days the habit was recorded.
 * @param totalDays       Total days since the habit was first recorded (inclusive of today).
 */
data class HabitConsistencyItem(
    val name: String,
    val color: HabitColor,
    val consistencyRate: Float,
    val activeDays: Int,
    val totalDays: Int,
)
