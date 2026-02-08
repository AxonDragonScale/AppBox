package com.axondragonscale.topty.widget.single

import android.content.Context
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.axondragonscale.topty.widget.common.SingleTokenWidgetState
import com.axondragonscale.topty.widget.common.WidgetStateProvider
import com.axondragonscale.topty.widget.common.WidgetUtils
import com.axondragonscale.topty.widget.common.actionRevealCode
import kotlinx.coroutines.delay
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Created by Ronak Harkhani on 30/12/24
 */
class SingleTokenGlanceWidget : GlanceAppWidget(), KoinComponent {

    private val stateProvider: WidgetStateProvider by inject()

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val widgetId = GlanceAppWidgetManager(context).getAppWidgetId(id)
        Log.d("SingleTokenWidget", "provideGlance: widgetId=$widgetId")
        val state = stateProvider.provideSingleWidgetState(widgetId)
        Log.d("SingleTokenWidget", "State loaded: error=${state.error}, token=${state.token?.issuer}, code=${state.code?.code}")

        provideContent {
            // If code is visible and not expired, schedule next update in 1 second
            val remainingSeconds = state.code?.let { WidgetUtils.calculateRemainingSeconds(it) } ?: 0
            if (state.code != null && remainingSeconds > 0) {
                LaunchedEffect(remainingSeconds) {
                    delay(1000)
                    update(context, id)
                }
            }

            GlanceTheme {
                SingleTokenWidgetContent(state)
            }
        }
    }

}

@Composable
fun SingleTokenWidgetContent(state: SingleTokenWidgetState) {
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(GlanceTheme.colors.surface)
            .cornerRadius(16.dp)
            .padding(16.dp)
            .clickable(onClick = actionRevealCode(state.widgetId, state.token?.id ?: 0L)),
        verticalAlignment = Alignment.Top,
        horizontalAlignment = Alignment.Start
    ) {
        when {
            state.error != null -> {
                // Error state
                Column(
                    modifier = GlanceModifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = state.error,
                        style = TextStyle(
                            fontSize = 14.sp,
                            color = GlanceTheme.colors.error
                        )
                    )
                }
            }

            else -> {
                // Issuer
                Text(
                    text = state.token?.issuer ?: "",
                    style = TextStyle(
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = GlanceTheme.colors.onSurface
                    )
                )

                // Name
                if (state.token?.name?.isNotBlank() == true) {
                    Spacer(modifier = GlanceModifier.height(4.dp))
                    Text(
                        text = state.token.name,
                        style = TextStyle(
                            fontSize = 12.sp,
                            color = GlanceTheme.colors.onSurfaceVariant
                        )
                    )
                }

                Spacer(modifier = GlanceModifier.height(12.dp))

                // Code or prompt
                if (state.code != null) {
                    // Show code
                    val formattedCode = WidgetUtils.formatCode(state.code.code)
                    Text(
                        text = formattedCode,
                        style = TextStyle(
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = GlanceTheme.colors.primary
                        )
                    )

                    Spacer(modifier = GlanceModifier.height(8.dp))

                    // Remaining time
                    val remainingSeconds = WidgetUtils.calculateRemainingSeconds(state.code)
                    Row(
                        modifier = GlanceModifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Simple progress indicator (text only for now - Glance has limited layout support)
                        Text(
                            text = "${remainingSeconds}s remaining",
                            style = TextStyle(
                                fontSize = 12.sp,
                                color = GlanceTheme.colors.onSurfaceVariant
                            )
                        )
                    }
                } else {
                    // Tap to reveal prompt
                    Text(
                        text = "Tap to reveal code",
                        style = TextStyle(
                            fontSize = 16.sp,
                            color = GlanceTheme.colors.onSurfaceVariant
                        )
                    )
                }
            }
        }
    }
}
