// Created by Ronak Harkhani on 03/07/25

package com.axondragonscale.limitless.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.axondragonscale.limitless.database.entity.GoalEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GoalDao {

    // MARK: Getters -------------------------------------------------------------------------------

    @Query("SELECT * FROM goal WHERE habitId = :habitId")
    fun getGoalsForHabit(habitId: Long): Flow<List<GoalEntity>>

    @Query("SELECT * FROM goal WHERE habitId = :habitId")
    suspend fun getGoalsForHabitOnce(habitId: Long): List<GoalEntity>

    // MARK: Upserts -------------------------------------------------------------------------------

    @Upsert
    suspend fun upsert(goal: GoalEntity): Long

    @Upsert
    suspend fun upsertAll(goals: List<GoalEntity>)

    // MARK: Delete --------------------------------------------------------------------------------

    @Query("DELETE FROM goal WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("DELETE FROM goal WHERE id IN (:ids)")
    suspend fun deleteByIds(ids: List<Long>)

    @Query("DELETE FROM goal WHERE habitId = :habitId")
    suspend fun deleteAllForHabit(habitId: Long)

}
