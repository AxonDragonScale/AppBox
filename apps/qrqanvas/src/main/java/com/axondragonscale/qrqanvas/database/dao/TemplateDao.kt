package com.axondragonscale.qrqanvas.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.axondragonscale.qrqanvas.database.entity.TemplateEntity
import kotlinx.coroutines.flow.Flow

/**
 * Created by Ronak Harkhani on 09/07/25
 */
@Dao
interface TemplateDao {

    @Insert
    suspend fun insert(entity: TemplateEntity): Long

    @Query("SELECT * FROM template ORDER BY createdAt DESC")
    fun getAll(): Flow<List<TemplateEntity>>

    @Query("UPDATE template SET name = :name WHERE id = :id")
    suspend fun updateName(id: Long, name: String)

    @Query("DELETE FROM template WHERE id = :id")
    suspend fun delete(id: Long)
}
