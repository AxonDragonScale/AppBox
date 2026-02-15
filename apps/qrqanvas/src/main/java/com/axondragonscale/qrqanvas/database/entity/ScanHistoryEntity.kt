package com.axondragonscale.qrqanvas.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.axondragonscale.qrqanvas.database.converter.QRConverters
import com.axondragonscale.qrqanvas.model.QRContent

/**
 * Created by Ronak Harkhani on 09/07/25
 */
@Entity(tableName = "scan_history")
@TypeConverters(QRConverters::class)
data class ScanHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val rawValue: String,
    val content: QRContent,
    val scannedAt: Long,
)
