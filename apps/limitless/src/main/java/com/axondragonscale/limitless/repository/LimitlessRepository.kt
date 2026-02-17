package com.axondragonscale.limitless.repository

import androidx.compose.ui.util.fastMaxOfOrDefault
import com.axondragonscale.limitless.database.dao.HabitDao
import com.axondragonscale.limitless.database.dao.HabitDayCountDao
import com.axondragonscale.limitless.database.dao.RecordDao
import com.axondragonscale.limitless.database.entity.HabitEntity
import com.axondragonscale.limitless.database.entity.RecordEntity
import com.axondragonscale.limitless.database.view.HabitDayCountView
import com.axondragonscale.limitless.model.HabitHeatMapData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.datetime.LocalDate

/**
 * Created by Ronak Harkhani on 09/11/24
 */
class LimitlessRepository(
    private val habitDao: HabitDao,
    private val recordDao: RecordDao,
    private val habitDayCountDao: HabitDayCountDao
) {

    fun getHabits(): Flow<List<HabitEntity>> = habitDao.getHabits()

    fun getRecords(): Flow<List<RecordEntity>> = recordDao.getRecords()

    fun getHabitsWithRecords(): Flow<Pair<List<HabitEntity>, List<RecordEntity>>> =
        combine(getHabits(), getRecords()) { habits, records -> Pair(habits, records) }

    suspend fun getHabit(id: Long) = habitDao.getHabit(id)

    suspend fun saveHabit(habit: HabitEntity) = habitDao.upsert(habit)

    suspend fun recordHabit(habitId: Long, message: String) =
        recordDao.upsert(RecordEntity.new(habitId, message))

    // MARK: Heat Map Data -------------------------------------------------------------------------

    /**
     * Get pre-aggregated day counts for all habits from the database view.
     * This is much more efficient than computing in memory.
     */
    fun getAllDayCounts(): Flow<List<HabitDayCountView>> =
        habitDayCountDao.getAllDayCounts()

    /**
     * Get pre-aggregated day counts for a specific habit.
     */
    fun getDayCountsForHabit(habitId: Long): Flow<List<HabitDayCountView>> =
        habitDayCountDao.getDayCountsForHabit(habitId)

    /**
     * Get day counts for a specific habit within a date range.
     * Useful for pagination or limiting data to visible range.
     */
    fun getDayCountsForDateRange(
        habitId: Long,
        startDate: LocalDate,
        endDate: LocalDate
    ): Flow<List<HabitDayCountView>> =
        habitDayCountDao.getDayCountsForDateRange(habitId, startDate, endDate)

    /**
     * Get heat map data for all habits.
     * Returns a Flow that automatically updates when habits or records change.
     * All data processing and transformation is handled here.
     */
    fun getHabitHeatMapData(): Flow<List<HabitHeatMapData>> =
        combine(
            getHabits(),
            getAllDayCounts()
        ) { habits, dayCounts ->
            // Group day counts by habitId for efficient lookup
            val dayCountsByHabit = dayCounts.groupBy { it.habitId }

            // Transform to HabitHeatMapData
            habits.map { habit ->
                val habitDayCounts = dayCountsByHabit[habit.id].orEmpty()
                val dayCountsMap = habitDayCounts.associate { dc -> dc.date to dc.count }
                val maxCount = habitDayCounts.fastMaxOfOrDefault(0) { it.count }

                HabitHeatMapData(habit.id, habit.name, dayCountsMap, maxCount)
            }
        }

}
