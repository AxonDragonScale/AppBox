package com.axondragonscale.qrqanvas.repository

import com.axondragonscale.qrqanvas.database.dao.ScanHistoryDao
import com.axondragonscale.qrqanvas.database.entity.ScanHistoryEntity
import kotlinx.coroutines.flow.Flow

/**
 * Created by Ronak Harkhani on 09/07/25
 */
class ScanHistoryRepository(private val scanHistoryDao: ScanHistoryDao) {

    fun getAll(): Flow<List<ScanHistoryEntity>> = scanHistoryDao.getAll()

    suspend fun insert(entity: ScanHistoryEntity): Long = scanHistoryDao.insert(entity)

    suspend fun delete(id: Long) = scanHistoryDao.delete(id)

    suspend fun deleteAll() = scanHistoryDao.deleteAll()
}
