package com.axondragonscale.topty.widget.common

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import com.axondragonscale.topty.repository.TokenRepository
import com.axondragonscale.topty.repository.WidgetRepository
import com.axondragonscale.topty.widget.single.SingleTokenGlanceWidget
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Created by Ronak Harkhani on 30/12/24
 */

/**
 * Action to reveal code in widget
 */
fun actionRevealCode(widgetId: Int, tokenId: Long) =
    actionRunCallback<RevealCodeAction>(
        parameters = actionParametersOf(
            RevealCodeAction.WIDGET_ID_KEY to widgetId,
            RevealCodeAction.TOKEN_ID_KEY to tokenId
        )
    )

/**
 * Callback that handles code reveal action
 */
class RevealCodeAction : ActionCallback, KoinComponent {

    companion object {
        val WIDGET_ID_KEY = ActionParameters.Key<Int>("widget_id")
        val TOKEN_ID_KEY = ActionParameters.Key<Long>("token_id")
    }

    private val widgetRepository: WidgetRepository by inject()
    private val tokenRepository: TokenRepository by inject()

    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        val widgetId = parameters[WIDGET_ID_KEY] ?: return
        val tokenId = parameters[TOKEN_ID_KEY] ?: return

        // Verify token exists in widget config
        val configuredTokenIds = widgetRepository.getWidgetTokenIds(widgetId)
        if (tokenId !in configuredTokenIds) return

        // Get the token
        val token = tokenRepository.getToken(tokenId) ?: return

        // Generate code and get expiry time
        val code = widgetRepository.generateCode(token)
        val expiry = widgetRepository.calculateNextExpiry(code)

        // Store expiry in memory
        WidgetMemoryState.setExpiry(widgetId, expiry)

        // Update widget UI
        SingleTokenGlanceWidget().update(context, glanceId)
    }
}
