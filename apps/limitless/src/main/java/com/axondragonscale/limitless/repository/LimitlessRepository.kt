package com.axondragonscale.limitless.repository

import androidx.compose.ui.util.fastMaxOfOrDefault
import com.axondragonscale.limitless.database.dao.GoalDao
import com.axondragonscale.limitless.database.dao.HabitDao
import com.axondragonscale.limitless.database.dao.HabitDayCountDao
import com.axondragonscale.limitless.database.dao.HabitHourCountDao
import com.axondragonscale.limitless.database.dao.RecordDao
import com.axondragonscale.limitless.database.dao.ReminderDao
import com.axondragonscale.limitless.database.entity.GoalEntity
import com.axondragonscale.limitless.database.entity.HabitEntity
import com.axondragonscale.limitless.database.entity.RecordEntity
import com.axondragonscale.limitless.database.entity.ReminderEntity
import com.axondragonscale.limitless.database.view.HabitDayCountView
import com.axondragonscale.limitless.database.view.HabitHourCountView
import com.axondragonscale.limitless.model.HabitHeatMapData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atTime
import kotlinx.datetime.toInstant

/**
 * Created by Ronak Harkhani on 09/11/24
 */
class LimitlessRepository(
    private val habitDao: HabitDao,
    private val recordDao: RecordDao,
    private val habitDayCountDao: HabitDayCountDao,
    private val habitHourCountDao: HabitHourCountDao,
    private val reminderDao: ReminderDao,
    private val goalDao: GoalDao,
) {

    fun getHabits(): Flow<List<HabitEntity>> = habitDao.getHabits()

    fun getRecords(): Flow<List<RecordEntity>> = recordDao.getRecords()

    fun getHabitsWithRecords(): Flow<Pair<List<HabitEntity>, List<RecordEntity>>> =
        combine(getHabits(), getRecords()) { habits, records -> Pair(habits, records) }

    suspend fun getHabit(id: Long) = habitDao.getHabit(id)

    fun getHabitFlow(id: Long) = habitDao.getHabitFlow(id)

    suspend fun saveHabit(habit: HabitEntity): Long = habitDao.upsert(habit)

    suspend fun getRecordsPaginated(habitId: Long, limit: Int, offset: Int): List<RecordEntity> =
        recordDao.getRecordsPaginated(habitId, limit, offset)

    suspend fun getRecordCount(habitId: Long): Int =
        recordDao.getRecordCount(habitId)

    fun getRecordsForDate(habitId: Long, date: LocalDate): Flow<List<RecordEntity>> =
        recordDao.getRecordsForDate(habitId, date.toString())

    suspend fun saveRecord(record: RecordEntity) = recordDao.upsert(record)

    suspend fun deleteRecord(id: Long) = recordDao.delete(id)

    suspend fun recordHabitForDate(habitId: Long, date: LocalDate) {
        // Use noon to avoid timezone boundary issues
        val timestamp = date.atTime(12, 0)
            .toInstant(TimeZone.currentSystemDefault())
            .toEpochMilliseconds()
        recordDao.upsert(RecordEntity(habitId = habitId, timestamp = timestamp, message = ""))
    }

    suspend fun deleteLatestRecordForDate(habitId: Long, date: LocalDate) {
        val dateString = date.toString() // "YYYY-MM-DD"
        val record = recordDao.getMostRecentRecordForDate(habitId, dateString)
        if (record != null) recordDao.delete(record.id)
    }

    // MARK: Goals ---------------------------------------------------------------------------------

    fun getGoalsForHabit(habitId: Long): Flow<List<GoalEntity>> =
        goalDao.getGoalsForHabit(habitId)

    suspend fun getGoalsForHabitOnce(habitId: Long): List<GoalEntity> =
        goalDao.getGoalsForHabitOnce(habitId)

    suspend fun saveGoal(goal: GoalEntity): Long = goalDao.upsert(goal)

    suspend fun saveGoals(goals: List<GoalEntity>) = goalDao.upsertAll(goals)

    suspend fun deleteGoal(goalId: Long) = goalDao.delete(goalId)

    suspend fun deleteGoals(goalIds: List<Long>) = goalDao.deleteByIds(goalIds)

    suspend fun deleteGoalsForHabit(habitId: Long) = goalDao.deleteAllForHabit(habitId)

    // MARK: Reminders -----------------------------------------------------------------------------

    fun getRemindersForHabit(habitId: Long): Flow<List<ReminderEntity>> =
        reminderDao.getRemindersForHabit(habitId)

    suspend fun getRemindersForHabitOnce(habitId: Long): List<ReminderEntity> =
        reminderDao.getRemindersForHabitOnce(habitId)

    suspend fun getReminder(id: Long): ReminderEntity? = reminderDao.getReminder(id)

    suspend fun getAllReminders(): List<ReminderEntity> = reminderDao.getAllReminders()

    suspend fun saveReminder(reminder: ReminderEntity): Long = reminderDao.upsert(reminder)

    suspend fun saveReminders(reminders: List<ReminderEntity>) = reminderDao.upsertAll(reminders)

    suspend fun deleteReminder(reminderId: Long) = reminderDao.delete(reminderId)

    suspend fun deleteReminders(reminderIds: List<Long>) = reminderDao.deleteByIds(reminderIds)

    suspend fun deleteRemindersForHabit(habitId: Long) = reminderDao.deleteAllForHabit(habitId)

    // MARK: Stats ---------------------------------------------------------------------------------

    /**
     * Get pre-aggregated hour-of-day counts for a specific habit.
     * Returns at most 24 rows (one per active hour).
     */
    fun getHourCountsForHabit(habitId: Long): Flow<List<HabitHourCountView>> =
        habitHourCountDao.getHourCountsForHabit(habitId)

    /**
     * Get total record count for a habit as a reactive Flow.
     */
    fun getRecordCountFlow(habitId: Long): Flow<Int> =
        recordDao.getRecordCountFlow(habitId)

    /**
     * Get the number of unique active days for a habit as a reactive Flow.
     */
    fun getActiveDayCount(habitId: Long): Flow<Int> =
        recordDao.getActiveDayCount(habitId)

    /**
     * Get the timestamp of the first record for a habit as a reactive Flow.
     * Returns null if no records exist.
     */
    fun getFirstRecordTimestamp(habitId: Long): Flow<Long?> =
        recordDao.getFirstRecordTimestamp(habitId)

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

                HabitHeatMapData(habit, dayCountsMap, maxCount)
            }
        }

}
