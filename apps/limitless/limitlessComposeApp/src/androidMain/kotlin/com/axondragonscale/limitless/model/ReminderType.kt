package com.axondragonscale.limitless.model

/**
 * Created by Ronak Harkhani on 26/06/25
 */
enum class ReminderType {
    ONE_TIME, DAILY, WEEKLY, MONTHLY;

    val displayName: String
        get() = when (this) {
            ONE_TIME -> "One Time"
            DAILY -> "Daily"
            WEEKLY -> "Weekly"
            MONTHLY -> "Monthly"
        }

    companion object {
        fun fromOrdinal(ordinal: Int) = entries[ordinal]
    }
}
