package com.axondragonscale.limitless.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import com.axondragonscale.limitless.database.entity.HabitEntity
import kotlinx.coroutines.flow.Flow

/**
 * Created by Ronak Harkhani on 02/11/24
 */
@Dao
interface HabitDao {

    // MARK: Getters -------------------------------------------------------------------------------

    @Query("SELECT * FROM habit")
    fun getHabits(): Flow<List<HabitEntity>>

    @Query("SELECT * FROM habit")
    suspend fun getAllHabits(): List<HabitEntity>

    @Query("SELECT * FROM habit WHERE id = :id")
    suspend fun getHabit(id: Long): HabitEntity?

    @Query("SELECT * FROM habit WHERE id = :id")
    fun getHabitFlow(id: Long): Flow<HabitEntity?>

    // MARK: Upserts -------------------------------------------------------------------------------

    @Upsert
    suspend fun upsert(habit: HabitEntity)

    // MARK: Delete --------------------------------------------------------------------------------

    @Query("DELETE FROM habit WHERE id = :id")
    suspend fun delete(id: Long)

    @Delete
    suspend fun delete(habit: HabitEntity)

    @Query("DELETE FROM habit")
    suspend fun clearAll()

}
