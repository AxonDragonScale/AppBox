package com.axondragonscale.limitless.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.axondragonscale.limitless.database.entity.ReminderEntity
import kotlinx.coroutines.flow.Flow

/**
 * Created by Ronak Harkhani on 26/06/25
 */
@Dao
interface ReminderDao {

    // MARK: Getters -------------------------------------------------------------------------------

    @Query("SELECT * FROM reminder WHERE habitId = :habitId")
    fun getRemindersForHabit(habitId: Long): Flow<List<ReminderEntity>>

    @Query("SELECT * FROM reminder WHERE habitId = :habitId")
    suspend fun getRemindersForHabitOnce(habitId: Long): List<ReminderEntity>

    @Query("SELECT * FROM reminder WHERE id = :id")
    suspend fun getReminder(id: Long): ReminderEntity?

    @Query("SELECT * FROM reminder")
    suspend fun getAllReminders(): List<ReminderEntity>

    // MARK: Upserts -------------------------------------------------------------------------------

    @Upsert
    suspend fun upsert(reminder: ReminderEntity): Long

    @Upsert
    suspend fun upsertAll(reminders: List<ReminderEntity>)

    // MARK: Delete --------------------------------------------------------------------------------

    @Query("DELETE FROM reminder WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("DELETE FROM reminder WHERE habitId = :habitId")
    suspend fun deleteAllForHabit(habitId: Long)

    @Query("DELETE FROM reminder WHERE id IN (:ids)")
    suspend fun deleteByIds(ids: List<Long>)

}
