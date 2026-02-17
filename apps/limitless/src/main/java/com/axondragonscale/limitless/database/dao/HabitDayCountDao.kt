package com.axondragonscale.limitless.database.dao

import androidx.room.Dao
import androidx.room.Query
import com.axondragonscale.limitless.database.view.HabitDayCountView
import com.axondragonscale.limitless.model.HabitMaxCount
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

/**
 * Created by Ronak Harkhani on 10/01/25
 */
@Dao
interface HabitDayCountDao {

    /**
     * Get day counts for a specific habit as a Flow.
     */
    @Query("SELECT * FROM habit_day_count_view WHERE habitId = :habitId ORDER BY date")
    fun getDayCountsForHabit(habitId: Long): Flow<List<HabitDayCountView>>

    /**
     * Get all day counts for all habits as a Flow.
     */
    @Query("SELECT * FROM habit_day_count_view ORDER BY habitId, date")
    fun getAllDayCounts(): Flow<List<HabitDayCountView>>

    /**
     * Get maximum count per habit for calculating heat map intensity.
     */
    @Query("""
        SELECT habitId, MAX(count) as maxCount
        FROM habit_day_count_view
        GROUP BY habitId
    """)
    suspend fun getMaxCountsPerHabit(): List<HabitMaxCount>

    /**
     * Get day counts for a specific habit within a date range.
     * Useful for pagination or limiting data to visible range.
     */
    @Query("""
        SELECT * FROM habit_day_count_view
        WHERE habitId = :habitId
        AND date >= :startDate
        AND date <= :endDate
        ORDER BY date
    """)
    fun getDayCountsForDateRange(
        habitId: Long,
        startDate: LocalDate,
        endDate: LocalDate
    ): Flow<List<HabitDayCountView>>


}
