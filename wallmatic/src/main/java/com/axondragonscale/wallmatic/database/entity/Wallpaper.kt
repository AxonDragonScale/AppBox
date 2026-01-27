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
) {
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0
}
