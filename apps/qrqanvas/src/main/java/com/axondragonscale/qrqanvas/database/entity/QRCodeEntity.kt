package com.axondragonscale.qrqanvas.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.axondragonscale.qrqanvas.database.converter.QRConverters
import com.axondragonscale.qrqanvas.model.QRContent
import com.axondragonscale.qrqanvas.model.QRStyle

/**
 * Created by Ronak Harkhani on 09/07/25
 */
@Entity(tableName = "qr_code")
@TypeConverters(QRConverters::class)
data class QRCodeEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val content: QRContent,
    val style: QRStyle,
    val createdAt: Long,
    val modifiedAt: Long,
)
