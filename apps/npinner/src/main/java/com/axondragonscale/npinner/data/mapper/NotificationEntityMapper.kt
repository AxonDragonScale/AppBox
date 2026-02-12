package com.axondragonscale.npinner.data.mapper

import com.axondragonscale.npinner.data.entity.NotificationEntity
import com.axondragonscale.npinner.model.NPinnerNotification

/**
 * Created by Ronak Harkhani on 30/04/23
 */
fun NPinnerNotification.toEntity() = toNotificationEntity(this)
fun List<NPinnerNotification>.toEntity() = this.map { it.toEntity() }
private fun toNotificationEntity(model: NPinnerNotification) = NotificationEntity(
    id = model.id,
    title = model.title,
    description = model.description,
    isPinned = model.isPinned,
    schedule = model.schedule,
    createdAt = model.createdAt,
    updatedAt = model.updatedAt,
    deletedAt = model.deletedAt,
)

fun NotificationEntity.toModel() = toNPinnerNotification(this)
fun List<NotificationEntity>.toModel() = this.map { it.toModel() }
private fun toNPinnerNotification(entity: NotificationEntity) = NPinnerNotification(
    id = entity.id,
    title = entity.title,
    description = entity.description,
    isPinned = entity.isPinned,
    schedule = entity.schedule,
    createdAt = entity.createdAt,
    updatedAt = entity.updatedAt,
    deletedAt = entity.deletedAt
)
