package com.axondragonscale.limitless.database.view

import androidx.room.DatabaseView
import kotlinx.datetime.LocalDate

/**
 * Created by Ronak Harkhani on 10/01/25
 *
 * Database view that pre-aggregates record counts by habit and date.
 * This view automatically updates when records are inserted/deleted.
 *
 * The date is stored as a String in the database (YYYY-MM-DD format) but is
 * automatically converted to LocalDate using the LocalDateConverter.
 */
@DatabaseView(
    viewName = "habit_day_count_view",
    value = """
        SELECT
            habitId,
            DATE(timestamp / 1000, 'unixepoch', 'localtime') as date,
            COUNT(*) as count
        FROM record
        GROUP BY habitId, date
    """
)
data class HabitDayCountView(
    val habitId: Long,
    val date: LocalDate,
    val count: Int
)
