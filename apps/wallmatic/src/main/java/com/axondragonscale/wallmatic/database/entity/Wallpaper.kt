package com.axondragonscale.wallmatic.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Created by Ronak Harkhani on 01/07/24
 */
@Entity(tableName = "wallpaper")
data class Wallpaper(
    val uri: String,
    var isBlacklisted: Boolean = false,
    // Crop hint coordinates (null = no crop, use full image)
    val cropLeft: Int? = null,
    val cropTop: Int? = null,
    val cropRight: Int? = null,
    val cropBottom: Int? = null,
    // Rotation in degrees (0, 90, 180, 270)
    val rotationDegrees: Int = 0,
) {
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0
}

/**
 * Helper extension to get crop rect from wallpaper entity.
 * Returns null if no crop is set.
 */
fun Wallpaper.getCropRect(): android.graphics.Rect? {
    return if (cropLeft != null && cropTop != null && cropRight != null && cropBottom != null) {
        android.graphics.Rect(cropLeft, cropTop, cropRight, cropBottom)
    } else null
}
