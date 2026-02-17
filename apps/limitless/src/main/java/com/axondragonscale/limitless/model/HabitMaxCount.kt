package com.axondragonscale.limitless.model

/**
 * Created by Ronak Harkhani on 10/01/25
 *
 * Represents the maximum count of records for a habit on any single day.
 * Used for calculating heat map intensity levels.
 */
data class HabitMaxCount(
    val habitId: Long,
    val maxCount: Int
)
