package com.axondragonscale.topty.ui.token

import com.axondragonscale.topty.database.entity.Token
import com.axondragonscale.topty.ui.Route

/**
 * Created by Ronak Harkhani on 14/10/24
 */
data class TokenUiState(
    val isLoading: Boolean = false,
    val tokenRoute: Route.Token,
    val token: Token = Token.emptyToken(),
) {
    val title = when (tokenRoute) {
        is Route.Token.Add -> "Add Token"
        is Route.Token.Edit -> "Edit Token"
    }
}
