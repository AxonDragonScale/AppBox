package com.axondragonscale.qrqanvas.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.axondragonscale.qrqanvas.database.converter.QRConverters
import com.axondragonscale.qrqanvas.model.QRStyle

/**
 * Created by Ronak Harkhani on 09/07/25
 */
@Entity(tableName = "template")
@TypeConverters(QRConverters::class)
data class TemplateEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val style: QRStyle,
    val createdAt: Long,
)
