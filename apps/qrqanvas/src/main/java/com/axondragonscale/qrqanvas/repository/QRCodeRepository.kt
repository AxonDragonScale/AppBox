package com.axondragonscale.qrqanvas.repository

import com.axondragonscale.qrqanvas.database.dao.QRCodeDao
import com.axondragonscale.qrqanvas.database.entity.QRCodeEntity
import kotlinx.coroutines.flow.Flow

/**
 * Created by Ronak Harkhani on 09/07/25
 */
class QRCodeRepository(private val qrCodeDao: QRCodeDao) {

    fun getAll(): Flow<List<QRCodeEntity>> = qrCodeDao.getAll()

    suspend fun getById(id: Long): QRCodeEntity? = qrCodeDao.getById(id)

    suspend fun insert(entity: QRCodeEntity): Long = qrCodeDao.insert(entity)

    suspend fun update(entity: QRCodeEntity) = qrCodeDao.update(entity)

    suspend fun delete(id: Long) = qrCodeDao.delete(id)
}
