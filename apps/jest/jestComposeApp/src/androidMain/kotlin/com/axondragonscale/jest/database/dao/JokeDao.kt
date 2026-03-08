package com.axondragonscale.jest.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.axondragonscale.jest.database.entity.JokeEntity
import kotlinx.coroutines.flow.Flow

/**
 * Created by Ronak Harkhani on 04/04/24
 */
@Dao
interface JokeDao {

    // Returns -1 in case of a conflict
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertJoke(joke: JokeEntity): Long

    @Query("SELECT * FROM joke ORDER BY timestamp DESC LIMIT 1")
    suspend fun getCurrentJoke(): JokeEntity?

    @Query("SELECT * FROM joke ORDER BY timestamp DESC")
    fun getAllJokes(): Flow<List<JokeEntity>>

    @Query("SELECT * FROM joke WHERE favorite = 1 ORDER BY timestamp DESC")
    fun getFavoriteJokes(): Flow<List<JokeEntity>>

    @Query("UPDATE joke set favorite = :favorite WHERE id = :id")
    suspend fun updateFavoriteFlag(id: Int, favorite: Boolean)

    @Query("SELECT * FROM joke")
    suspend fun getJokes(): List<JokeEntity>

    @Query("DELETE FROM joke WHERE id = :id")
    suspend fun deleteJoke(id: Int)

    @Query("DELETE FROM joke WHERE 1")
    suspend fun deleteAllJokes()

}
