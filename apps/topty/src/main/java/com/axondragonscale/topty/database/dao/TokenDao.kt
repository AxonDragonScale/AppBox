package com.axondragonscale.topty.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import com.axondragonscale.topty.database.entity.Token
import kotlinx.coroutines.flow.Flow

/**
 * Created by Ronak Harkhani on 14/10/24
 */
@Dao
interface TokenDao {

    // MARK: Getters -------------------------------------------------------------------------------

    @Query("SELECT * FROM token")
    fun getTokens(): Flow<List<Token>>

    @Query("SELECT * FROM token")
    suspend fun getAllTokens(): List<Token>

    @Query("SELECT * FROM token WHERE id = :id")
    suspend fun getToken(id: Long): Token?


    // MARK: Insert/Update -------------------------------------------------------------------------

    @Upsert
    suspend fun upsert(token: Token): Long


    // MARK: Delete --------------------------------------------------------------------------------

    @Delete
    suspend fun delete(token: Token)

    @Query("DELETE FROM token WHERE id = :id")
    suspend fun delete(id: Long)

}
