package com.axondragonscale.qrqanvas.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.axondragonscale.qrqanvas.database.entity.ScanHistoryEntity
import kotlinx.coroutines.flow.Flow

/**
 * Created by Ronak Harkhani on 09/07/25
 */
@Dao
interface ScanHistoryDao {

    @Insert
    suspend fun insert(entity: ScanHistoryEntity): Long

    @Query("SELECT * FROM scan_history ORDER BY scannedAt DESC")
    fun getAll(): Flow<List<ScanHistoryEntity>>

    @Query("SELECT * FROM scan_history WHERE id = :id")
    suspend fun getById(id: Long): ScanHistoryEntity?

    @Query("DELETE FROM scan_history WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("DELETE FROM scan_history")
    suspend fun deleteAll()
}
