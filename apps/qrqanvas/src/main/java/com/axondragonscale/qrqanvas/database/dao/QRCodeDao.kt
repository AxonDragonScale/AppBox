package com.axondragonscale.qrqanvas.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.axondragonscale.qrqanvas.database.entity.QRCodeEntity
import kotlinx.coroutines.flow.Flow

/**
 * Created by Ronak Harkhani on 09/07/25
 */
@Dao
interface QRCodeDao {

    @Insert
    suspend fun insert(entity: QRCodeEntity): Long

    @Query("SELECT * FROM qr_code ORDER BY modifiedAt DESC")
    fun getAll(): Flow<List<QRCodeEntity>>

    @Query("SELECT * FROM qr_code WHERE id = :id")
    suspend fun getById(id: Long): QRCodeEntity?

    @Update
    suspend fun update(entity: QRCodeEntity)

    @Query("DELETE FROM qr_code WHERE id = :id")
    suspend fun delete(id: Long)
}
