package com.axondragonscale.topty.ui.qrscanner

/**
 * Created by Ronak Harkhani on 27/10/24
 */
sealed interface QRScannerUiEffect {
    data class NavigateToAddToken(val tokenUri: String) : QRScannerUiEffect
}
