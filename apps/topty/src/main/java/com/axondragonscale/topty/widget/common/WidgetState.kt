package com.axondragonscale.topty.widget.common

import com.axondragonscale.topty.database.entity.Token
import com.axondragonscale.topty.model.Code

/**
 * Created by Ronak Harkhani on 29/12/24
 */

/**
 * State for single token widget
 */
data class SingleTokenWidgetState(
    val widgetId: Int,
    val token: Token?,
    val code: Code?,  // null when hidden
    val error: String? = null,  // "Token not found" or "No token configured"
)

/**
 * State for multi token widget
 */
data class MultiTokenWidgetState(
    val widgetId: Int,
    val tokens: List<TokenWidgetItem>,
    val isEmpty: Boolean,
)

/**
 * Individual token item in multi token widget
 */
data class TokenWidgetItem(
    val token: Token,
    val code: Code?,  // null when hidden
)
