package com.axondragonscale.qrqanvas.ui.home

/**
 * Created by Ronak Harkhani on 09/07/25
 */
sealed interface HomeUiEffect {
    data object NavigateToCreator : HomeUiEffect
    data object NavigateToScanner : HomeUiEffect
    data object NavigateToHistory : HomeUiEffect
    data object NavigateToSettings : HomeUiEffect
    data class NavigateToDetail(val qrCodeId: Long) : HomeUiEffect
}
