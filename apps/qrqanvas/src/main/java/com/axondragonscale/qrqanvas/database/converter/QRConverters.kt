package com.axondragonscale.qrqanvas.database.converter

import androidx.room.TypeConverter
import com.axondragonscale.qrqanvas.model.QRContent
import com.axondragonscale.qrqanvas.model.QRStyle
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Created by Ronak Harkhani on 09/07/25
 */
object QRConverters {

    private val json = Json { ignoreUnknownKeys = true }

    @TypeConverter
    fun fromQRStyle(style: QRStyle): String = json.encodeToString(style)

    @TypeConverter
    fun toQRStyle(value: String): QRStyle = json.decodeFromString(value)

    @TypeConverter
    fun fromQRContent(content: QRContent): String = json.encodeToString(content)

    @TypeConverter
    fun toQRContent(value: String): QRContent = json.decodeFromString(value)
}
