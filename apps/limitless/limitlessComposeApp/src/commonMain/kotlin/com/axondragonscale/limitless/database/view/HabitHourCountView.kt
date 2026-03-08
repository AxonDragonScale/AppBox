package com.axondragonscale.limitless.database.view

import androidx.room.DatabaseView

/**
 * Created by Ronak Harkhani on 21/07/25
 *
 * Database view that pre-aggregates record counts by habit and hour of day.
 * This view automatically updates when records are inserted/deleted.
 *
 * Used by the Stats tab's Time of Day histogram to show when the user
 * typically performs a habit, without pulling all raw records into memory.
 * Returns at most 24 rows per habit (one per active hour).
 */
@DatabaseView(
    viewName = "habit_hour_count_view",
    value = """
        SELECT
            habitId,
            CAST(STRFTIME('%H', timestamp / 1000, 'unixepoch', 'localtime') AS INTEGER) AS hour,
            COUNT(*) AS count
        FROM record
        GROUP BY habitId, hour
    """
)
data class HabitHourCountView(
    val habitId: Long,
    val hour: Int,
    val count: Int,
)
