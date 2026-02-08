package com.axondragonscale.topty.repository

import com.axondragonscale.topty.core.HOTPGenerator
import com.axondragonscale.topty.core.TOTPGenerator
import com.axondragonscale.topty.database.dao.WidgetDao
import com.axondragonscale.topty.database.entity.Token
import com.axondragonscale.topty.database.entity.Widget
import com.axondragonscale.topty.model.WidgetType
import com.axondragonscale.topty.model.Code
import com.axondragonscale.topty.model.Protocol
import com.axondragonscale.topty.model.TOTPCode
import com.axondragonscale.topty.util.generateCode
import com.axondragonscale.topty.widget.common.WidgetMemoryState
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Created by Ronak Harkhani on 29/12/24
 */
class WidgetRepository(
    private val widgetDao: WidgetDao,
    private val tokenRepository: TokenRepository,
) {

    // Widget Configuration
    suspend fun saveWidget(widgetId: Int, type: WidgetType, tokenIds: List<Long>) {
        val widget = Widget(
            widgetId = widgetId,
            widgetType = type,
            tokenIds = Json.encodeToString(tokenIds)
        )
        widgetDao.upsert(widget)
    }

    suspend fun getWidget(widgetId: Int): Widget? {
        return widgetDao.getWidget(widgetId)
    }

    suspend fun deleteWidget(widgetId: Int) {
        widgetDao.delete(widgetId)
        // Also clear from memory
        WidgetMemoryState.clearExpiry(widgetId)
    }

    suspend fun getWidgetTokenIds(widgetId: Int): List<Long> {
        val widget = widgetDao.getWidget(widgetId) ?: return emptyList()
        return Json.decodeFromString(widget.tokenIds)
    }

    suspend fun generateCode(token: Token): Code {
        return token.generateCode()
    }

    // Calculate next expiry based on Code object
    // For TOTP: use periodEnd from code
    // For HOTP: fixed 30 seconds from now
    fun calculateNextExpiry(code: Code): Long {
        return when (code) {
            is TOTPCode -> code.periodEnd
            else -> System.currentTimeMillis() + 30_000 // HOTP: 30 seconds
        }
    }
}
