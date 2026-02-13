package com.axondragonscale.qrqanvas.ui.creator

/**
 * Created by Ronak Harkhani on 09/07/25
 */
sealed interface CreatorUiEffect {
    data object SaveCompleted : CreatorUiEffect
    data class ShowSnackbar(val message: String) : CreatorUiEffect
}
