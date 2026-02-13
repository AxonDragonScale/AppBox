package com.axondragonscale.qrqanvas.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Created by Ronak Harkhani on 09/07/25
 */
@Entity(tableName = "qr_code")
data class QRCodeEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val contentJson: String,
    val styleJson: String,
    val createdAt: Long,
    val modifiedAt: Long,
)
