package com.axondragonscale.topty.widget.single

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import com.axondragonscale.topty.repository.WidgetRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Created by Ronak Harkhani on 30/12/24
 */
class SingleTokenWidgetReceiver : GlanceAppWidgetReceiver(), KoinComponent {

    override val glanceAppWidget: GlanceAppWidget = SingleTokenGlanceWidget()

    private val widgetRepository: WidgetRepository by inject()

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        // Glance handles updates automatically
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        super.onDeleted(context, appWidgetIds)

        // Clean up widget configs and memory state
        appWidgetIds.forEach { widgetId ->
            CoroutineScope(Dispatchers.IO).launch {
                widgetRepository.deleteWidget(widgetId)
            }
        }
    }

    companion object {
        suspend fun updateWidget(context: Context, appWidgetManager: AppWidgetManager, widgetId: Int) {
            val glanceId = GlanceAppWidgetManager(context).getGlanceIdBy(widgetId)
            android.util.Log.d("SingleTokenWidgetReceiver", "Updating widget $widgetId with glanceId $glanceId")
            SingleTokenGlanceWidget().update(context, glanceId)
            android.util.Log.d("SingleTokenWidgetReceiver", "Widget update completed for $widgetId")
        }

        fun updateAllWidgets(context: Context) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val widgetIds = appWidgetManager.getAppWidgetIds(
                ComponentName(context, SingleTokenWidgetReceiver::class.java)
            )
            CoroutineScope(Dispatchers.Main).launch {
                widgetIds.forEach { updateWidget(context, appWidgetManager, it) }
            }
        }
    }
}
