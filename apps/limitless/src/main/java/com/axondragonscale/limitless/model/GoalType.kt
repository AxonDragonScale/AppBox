// Created by Ronak Harkhani on 03/07/25

package com.axondragonscale.limitless.model

enum class GoalType {
    DAILY, WEEKLY, MONTHLY;

    val displayName: String
        get() = when (this) {
            DAILY -> "Daily"
            WEEKLY -> "Weekly"
            MONTHLY -> "Monthly"
        }

    val unit: String
        get() = when (this) {
            DAILY -> "day"
            WEEKLY -> "week"
            MONTHLY -> "month"
        }

    companion object {
        fun fromOrdinal(ordinal: Int) = entries[ordinal]
    }
}
