package com.axondragonscale.jest.widget

import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver

/**
 * Created by Ronak Harkhani on 20/04/24
 */
class JestWidgetReceiver: GlanceAppWidgetReceiver() {

    override val glanceAppWidget: GlanceAppWidget
        get() = JestAppWidget()

}
