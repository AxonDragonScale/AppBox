package com.axondragonscale.wallmatic.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Created by Ronak Harkhani on 26/06/24
 */
@Entity(tableName = "folder")
data class Folder(
    val name: String,
    var coverUri: String?,
    val folderUri: String,
    var wallpapers: List<Int>,
) {
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0
}
