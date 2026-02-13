package com.axondragonscale.qrqanvas.database.converter

import androidx.room.TypeConverter
import com.axondragonscale.commons.CommonJson
import com.axondragonscale.qrqanvas.model.QRContent
import com.axondragonscale.qrqanvas.model.QRStyle

/**
 * Created by Ronak Harkhani on 09/07/25
 */
object QRConverters {

    @TypeConverter
    fun fromQRStyle(style: QRStyle): String = CommonJson.encodeToString(style)

    @TypeConverter
    fun toQRStyle(value: String): QRStyle = CommonJson.decodeFromString(value)

    @TypeConverter
    fun fromQRContent(content: QRContent): String = CommonJson.encodeToString(content)

    @TypeConverter
    fun toQRContent(value: String): QRContent = CommonJson.decodeFromString(value)
}
