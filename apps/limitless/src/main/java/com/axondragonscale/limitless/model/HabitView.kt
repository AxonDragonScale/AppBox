// Created by Ronak Harkhani on 04/03/26

package com.axondragonscale.limitless.model

/**
 * The view type a user can choose for displaying their habit on the home screen.
 *
 * - [HEAT_MAP]: A 26-week scrollable contribution-style grid (default).
 * - [MONTH]: A read-only calendar grid showing the current calendar month.
 * - [WEEK]: A rolling 7-day strip ending on today.
 */
enum class HabitView {
    HEAT_MAP,
    MONTH,
    WEEK,
    ;

    val displayName: String
        get() = when (this) {
            HEAT_MAP -> "Heat Map"
            MONTH    -> "Month"
            WEEK     -> "Week"
        }

    companion object {
        val DEFAULT get() = HEAT_MAP
        fun fromOrdinal(ordinal: Int) = entries[ordinal]
    }
}
