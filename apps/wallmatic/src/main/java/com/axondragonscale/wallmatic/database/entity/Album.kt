package com.axondragonscale.wallmatic.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Created by Ronak Harkhani on 26/06/24
 */
@Entity(tableName = "album")
data class Album(
    val name: String,
) {
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0
}
