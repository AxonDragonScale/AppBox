package com.axondragonscale.wallmatic.database.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.axondragonscale.wallmatic.model.WallpaperCrop

/**
 * Created by Ronak Harkhani on 01/07/24
 */
@Entity(tableName = "wallpaper")
data class Wallpaper(
    val uri: String,
    val albumId: Int,
    val folderId: Int? = null,
    var isBlacklisted: Boolean = false,
    @Embedded val crop: WallpaperCrop = WallpaperCrop.Default,
) {
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0

    val hasCustomCrop: Boolean get() = !crop.isDefault
}
