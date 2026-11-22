package com.axondragonscale.commons.devtools.logs.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/** Created by Ronak Harkhani on 11/21/26 */

/** One captured log record; [message] is kept whole. */
@Entity(tableName = "logs")
internal data class DevLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long,
    val priority: String,
    val tag: String,
    val message: String,
    val session: String,
)
