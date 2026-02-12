package com.axondragonscale.npinner.repository

import com.axondragonscale.npinner.data.dao.NotificationDao
import com.axondragonscale.npinner.data.mapper.toEntity
import com.axondragonscale.npinner.data.mapper.toModel
import com.axondragonscale.npinner.model.NPinnerNotification
import kotlinx.coroutines.flow.map

/**
 * Created by Ronak Harkhani on 30/04/23
 */
class NotificationRepository(
    private val notificationDao: NotificationDao,
) {

    suspend fun upsert(notification: NPinnerNotification) {
        val updatedNotification = notification.copy(updatedAt = System.currentTimeMillis())
        notificationDao.upsert(updatedNotification.toEntity())
    }

    fun getNotifications() =
        notificationDao.getNotifications().map { it.toModel() }

    suspend fun getPinnedNotifications() =
        notificationDao.getPinnedNotifications().toModel()

    suspend fun getNotification(id: String) =
        notificationDao.getNotification(id).toModel()

    suspend fun updatePinStatus(id: String, isPinned: Boolean) {
        notificationDao.updatePinStatus(id, isPinned, System.currentTimeMillis())
    }

    suspend fun delete(notification: NPinnerNotification) {
        val timestamp = System.currentTimeMillis()
        val deletedNotification = notification.copy(
            updatedAt = timestamp,
            deletedAt = timestamp,
        )
        notificationDao.upsert(deletedNotification.toEntity())
    }
}
