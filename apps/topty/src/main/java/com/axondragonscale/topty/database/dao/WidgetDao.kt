package com.axondragonscale.topty.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.axondragonscale.topty.database.entity.Widget
import com.axondragonscale.topty.model.WidgetType

/**
 * Created by Ronak Harkhani on 29/12/24
 */
@Dao
interface WidgetDao {

    @Query("SELECT * FROM widget WHERE widgetId = :widgetId")
    suspend fun getWidget(widgetId: Int): Widget?

    @Query("SELECT * FROM widget WHERE widgetType = :type")
    suspend fun getWidgetsByType(type: WidgetType): List<Widget>

    @Query("SELECT * FROM widget")
    suspend fun getAllWidgets(): List<Widget>

    @Upsert
    suspend fun upsert(widget: Widget)

    @Query("DELETE FROM widget WHERE widgetId = :widgetId")
    suspend fun delete(widgetId: Int)

    @Query("DELETE FROM widget")
    suspend fun deleteAll()
}
