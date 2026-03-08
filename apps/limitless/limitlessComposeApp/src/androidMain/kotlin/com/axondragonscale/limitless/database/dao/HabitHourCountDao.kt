package com.axondragonscale.limitless.database.dao

import androidx.room.Dao
import androidx.room.Query
import com.axondragonscale.limitless.database.view.HabitHourCountView
import kotlinx.coroutines.flow.Flow

/**
 * Created by Ronak Harkhani on 21/07/25
 */
@Dao
interface HabitHourCountDao {

    /**
     * Get hour-of-day counts for a specific habit as a Flow.
     * Returns at most 24 rows (one per active hour).
     */
    @Query("SELECT * FROM habit_hour_count_view WHERE habitId = :habitId ORDER BY hour")
    fun getHourCountsForHabit(habitId: Long): Flow<List<HabitHourCountView>>

}
