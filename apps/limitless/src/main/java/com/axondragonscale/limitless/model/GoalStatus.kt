// Created by Ronak Harkhani on 26/06/25

package com.axondragonscale.limitless.model

import com.axondragonscale.limitless.database.entity.GoalEntity

/**
 * Pairs a [GoalEntity] with its computed progress for the current period.
 *
 * - [current] is the number of times the habit was recorded in the goal's period so far.
 * - [isAchieved] is true when [current] meets or exceeds the target.
 * - [progress] is a 0f..1f fraction suitable for a progress indicator.
 */
data class GoalStatus(
    val goal: GoalEntity,
    val current: Int,
) {
    val isAchieved: Boolean get() = current >= goal.target
    val progress: Float get() = (current.toFloat() / goal.target).coerceAtMost(1f)
}
