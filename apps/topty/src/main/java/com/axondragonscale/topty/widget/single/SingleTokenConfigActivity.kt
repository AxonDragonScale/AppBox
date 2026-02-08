package com.axondragonscale.topty.widget.single

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Surface
import androidx.lifecycle.lifecycleScope
import com.axondragonscale.topty.model.WidgetType
import com.axondragonscale.topty.repository.WidgetRepository
import com.axondragonscale.topty.ui.theme.ToptyTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

/**
 * Created by Ronak Harkhani on 30/12/24
 */
class SingleTokenConfigActivity : ComponentActivity() {

    private val widgetRepository: WidgetRepository by inject()
    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Get widget ID
        appWidgetId = intent?.extras?.getInt(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID

        // Set result to CANCELED initially
        setResult(RESULT_CANCELED)

        Log.d("SingleTokenConfig", "onCreate: appWidgetId=$appWidgetId")

        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            Log.e("SingleTokenConfig", "Invalid widget ID, finishing")
            finish()
            return
        }

        setContent {
            ToptyTheme {
                Surface {
                    SingleTokenConfigScreen(
                        onTokenSelected = { tokenId -> saveConfigAndFinish(tokenId) },
                        onCancel = { finish() }
                    )
                }
            }
        }
    }

    private fun saveConfigAndFinish(tokenId: Long) {
        Log.d("SingleTokenConfig", "saveConfigAndFinish: widgetId=$appWidgetId, tokenId=$tokenId")
        lifecycleScope.launch {
            // Save widget config
            widgetRepository.saveWidget(appWidgetId, WidgetType.SINGLE, listOf(tokenId))
            Log.d("SingleTokenConfig", "Widget config saved")

            // Small delay to ensure database write is fully committed
            delay(100)

            // Trigger widget update via receiver's update method
            val appWidgetManager = AppWidgetManager.getInstance(this@SingleTokenConfigActivity)
            SingleTokenWidgetReceiver.updateWidget(this@SingleTokenConfigActivity, appWidgetManager, appWidgetId)
            Log.d("SingleTokenConfig", "Widget update triggered via receiver")

            // Return success
            val resultValue = Intent().apply {
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            }
            setResult(RESULT_OK, resultValue)
            finish()
        }
    }
}
