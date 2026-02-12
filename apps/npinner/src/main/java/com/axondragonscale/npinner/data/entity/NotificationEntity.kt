package com.axondragonscale.npinner.data.entity

import androidx.annotation.Keep
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.axondragonscale.npinner.model.Schedule

/**
 * Created by Ronak Harkhani on 30/04/23
 */

@Keep
@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val isPinned: Boolean = true,
    @Embedded val schedule: Schedule? = null,
    val createdAt: Long,
    val updatedAt: Long,
    val deletedAt: Long? = null,
)
