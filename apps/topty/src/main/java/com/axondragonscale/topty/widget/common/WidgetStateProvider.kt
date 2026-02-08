package com.axondragonscale.topty.widget.common

import android.util.Log
import com.axondragonscale.topty.repository.TokenRepository
import com.axondragonscale.topty.repository.WidgetRepository
import kotlinx.serialization.json.Json

/**
 * Created by Ronak Harkhani on 29/12/24
 */
class WidgetStateProvider(
    private val widgetRepository: WidgetRepository,
    private val tokenRepository: TokenRepository,
) {

    suspend fun provideSingleWidgetState(widgetId: Int): SingleTokenWidgetState {
        Log.d("WidgetStateProvider", "provideSingleWidgetState: widgetId=$widgetId")
        val widget = widgetRepository.getWidget(widgetId)
        if (widget == null) {
            Log.d("WidgetStateProvider", "Widget not found in database")
            return SingleTokenWidgetState(widgetId = widgetId, token = null, code = null, error = "Widget not configured")
        }
        Log.d("WidgetStateProvider", "Widget found: type=${widget.widgetType}, tokenIds=${widget.tokenIds}")

        val tokenIds = Json.decodeFromString<List<Long>>(widget.tokenIds)
        Log.d("WidgetStateProvider", "Decoded tokenIds: $tokenIds")
        val tokenId = tokenIds.firstOrNull()
        if (tokenId == null) {
            Log.d("WidgetStateProvider", "No token ID in configuration")
            return SingleTokenWidgetState(widgetId = widgetId, token = null, code = null, error = "No token configured")
        }
        Log.d("WidgetStateProvider", "Using tokenId: $tokenId")

        val token = tokenRepository.getToken(tokenId)
        if (token == null) {
            Log.d("WidgetStateProvider", "Token not found in database: tokenId=$tokenId")
            return SingleTokenWidgetState(widgetId = widgetId, token = null, code = null, error = "Token not found")
        }
        Log.d("WidgetStateProvider", "Token found: issuer=${token.issuer}, name=${token.name}")

        // Check if code should be visible (from memory)
        val isCodeVisible = WidgetMemoryState.isCodeVisible(widgetId)
        Log.d("WidgetStateProvider", "Code visible: $isCodeVisible")
        val code = if (isCodeVisible) {
            widgetRepository.generateCode(token)
        } else {
            null
        }

        Log.d("WidgetStateProvider", "Returning state: token=${token.issuer}, hasCode=${code != null}")
        return SingleTokenWidgetState(
            widgetId = widgetId,
            token = token,
            code = code,
            error = null
        )
    }

    suspend fun provideMultiWidgetState(widgetId: Int): MultiTokenWidgetState {
        val widget = widgetRepository.getWidget(widgetId)
            ?: return MultiTokenWidgetState(
                widgetId = widgetId,
                tokens = emptyList(),
                isEmpty = true
            )

        val tokenIds = Json.decodeFromString<List<Long>>(widget.tokenIds)
        if (tokenIds.isEmpty()) {
            return MultiTokenWidgetState(
                widgetId = widgetId,
                tokens = emptyList(),
                isEmpty = true
            )
        }

        // Check if code should be visible (from memory)
        val isCodeVisible = WidgetMemoryState.isCodeVisible(widgetId)

        val tokens = tokenIds.mapNotNull { tokenId ->
            val token = tokenRepository.getToken(tokenId) ?: return@mapNotNull null

            val code = if (isCodeVisible) {
                widgetRepository.generateCode(token)
            } else {
                null
            }

            TokenWidgetItem(
                token = token,
                code = code
            )
        }

        return MultiTokenWidgetState(
            widgetId = widgetId,
            tokens = tokens,
            isEmpty = tokens.isEmpty()
        )
    }
}
