package com.axondragonscale.wallmatic.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Created by Ronak Harkhani on 26/06/24
 */
@Entity(tableName = "folder")
data class Folder(
    val name: String,
    val folderUri: String,
    val albumId: Int,
) {
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0
}
