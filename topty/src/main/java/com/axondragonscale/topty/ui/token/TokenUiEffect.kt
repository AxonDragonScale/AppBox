package com.axondragonscale.topty.ui.token

/**
 * Created by Ronak Harkhani on 28/10/24
 */
sealed interface TokenUiEffect {
    data object NavigateToHome: TokenUiEffect
}
