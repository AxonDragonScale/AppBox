package com.axondragonscale.qrqanvas.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Created by Ronak Harkhani on 09/07/25
 */
@Entity(tableName = "template")
data class TemplateEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val styleJson: String,
    val createdAt: Long,
)
