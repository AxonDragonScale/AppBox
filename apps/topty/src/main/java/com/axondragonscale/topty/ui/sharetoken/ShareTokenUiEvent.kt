package com.axondragonscale.topty.ui.sharetoken

/**
 * Created by Ronak Harkhani on 28/12/24
 */
sealed interface ShareTokenUiEvent {
    data object CopyUri : ShareTokenUiEvent
    data object ShareUri : ShareTokenUiEvent
}
