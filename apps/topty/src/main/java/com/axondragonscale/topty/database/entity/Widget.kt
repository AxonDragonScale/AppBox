package com.axondragonscale.topty.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.axondragonscale.topty.model.WidgetType

/**
 * Created by Ronak Harkhani on 29/12/24
 */
@Entity(tableName = "widget")
data class Widget(
    @PrimaryKey val widgetId: Int,
    val widgetType: WidgetType,
    val tokenIds: String, // JSON array: "[1]" for single, "[1,2,3]" for multi
    val createdAt: Long = System.currentTimeMillis(),
)

