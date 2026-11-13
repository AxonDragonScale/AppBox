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
    val cropFocalX: Float = 0.5f,
    val cropFocalY: Float = 0.5f,
    val cropZoom: Float = 1f,
) {
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0

    /** True once the crop is off the centred 1x default. */
    val hasCustomCrop: Boolean
        get() = cropZoom > 1f || cropFocalX != 0.5f || cropFocalY != 0.5f
}
