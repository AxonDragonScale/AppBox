/** Created by Ronak on 05/30/26 */
package com.axondragonscale.cardy.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.axondragonscale.cardy.database.entity.CardEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CardDao {

    @Query("SELECT * FROM cards ORDER BY timestamp DESC")
    fun getAllCards(): Flow<List<CardEntity>>

    @Query("SELECT * FROM cards WHERE id = :id")
    suspend fun getCardById(id: Long): CardEntity?

    @Insert
    suspend fun insertCard(card: CardEntity): Long

    @Update
    suspend fun updateCard(card: CardEntity)

    @Query("DELETE FROM cards WHERE id = :id")
    suspend fun deleteCard(id: Long)

    @Query("DELETE FROM cards")
    suspend fun deleteAllCards()
}
