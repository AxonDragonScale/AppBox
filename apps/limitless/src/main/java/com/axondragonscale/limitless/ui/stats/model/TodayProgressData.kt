package com.axondragonscale.limitless.ui.stats.model

import com.axondragonscale.limitless.model.HabitColor

/**
 * Created by Ronak Harkhani on 23/07/25
 *
 * Data for the Today's Progress section.
 * Shows how many habits have been recorded today out of the total active habits.
 *
 * @param completed Number of habits recorded at least once today.
 * @param total     Total number of active habits.
 * @param habits    Per-habit completion status for the current day.
 */
data class TodayProgressData(
    val completed: Int,
    val total: Int,
    val habits: List<TodayHabitStatus>,
)

/**
 * Completion status for a single habit on the current day.
 *
 * @param name        Habit display name.
 * @param color       Habit color, used to tint the status indicator.
 * @param isCompleted True if the habit has at least one record today.
 */
data class TodayHabitStatus(
    val name: String,
    val color: HabitColor,
    val isCompleted: Boolean,
)
