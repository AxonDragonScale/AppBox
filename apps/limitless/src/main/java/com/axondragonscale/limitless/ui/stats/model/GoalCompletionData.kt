package com.axondragonscale.limitless.ui.stats.model

import com.axondragonscale.limitless.model.GoalType

/**
 * Created by Ronak Harkhani on 21/07/25
 *
 * Data for the Goal Completion Rate ring chart widget.
 * Only present when the habit has goals configured.
 *
 * @param rates Completion rate per goal type, ordered by [GoalType] ordinal.
 */
data class GoalCompletionData(
    val rates: List<GoalCompletionRate>,
)

/**
 * Completion rate for a single goal type.
 *
 * @param type The goal period type (Daily, Weekly, Monthly).
 * @param target The target count per period.
 * @param percentage Fraction of periods where the goal was met (0f..1f).
 */
data class GoalCompletionRate(
    val type: GoalType,
    val target: Int,
    val percentage: Float,
)
