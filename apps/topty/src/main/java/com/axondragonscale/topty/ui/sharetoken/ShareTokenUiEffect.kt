package com.axondragonscale.topty.ui.sharetoken

/**
 * Created by Ronak Harkhani on 28/12/24
 */
sealed interface ShareTokenUiEffect {
    data class ShowMessage(val message: String) : ShareTokenUiEffect
    data class ShareUri(val uri: String) : ShareTokenUiEffect
}
