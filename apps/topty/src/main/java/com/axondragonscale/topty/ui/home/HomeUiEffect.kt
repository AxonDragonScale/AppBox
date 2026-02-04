package com.axondragonscale.topty.ui.home

/**
 * Created by Ronak Harkhani on 30/10/24
 */
sealed interface HomeUiEffect {
    data class NavigateToAddToken(val tokenUri: String) : HomeUiEffect
    data object ShowNoQRFoundDialog : HomeUiEffect
}
