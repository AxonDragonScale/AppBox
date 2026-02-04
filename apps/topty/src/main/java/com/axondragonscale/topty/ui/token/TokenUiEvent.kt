package com.axondragonscale.topty.ui.token

import com.axondragonscale.topty.database.entity.Token

/**
 * Created by Ronak Harkhani on 14/10/24
 */
sealed interface TokenUiEvent {
    data object NavigateBack: TokenUiEvent
    data object Save: TokenUiEvent
    data class UpdateToken(val token: Token): TokenUiEvent
}
