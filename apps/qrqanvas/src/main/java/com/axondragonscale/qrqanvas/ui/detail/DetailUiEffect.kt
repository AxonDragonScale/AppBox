package com.axondragonscale.qrqanvas.ui.detail

/**
 * Created by Ronak Harkhani on 09/07/25
 */
sealed interface DetailUiEffect {
    data class NavigateToEditor(val qrCodeId: Long) : DetailUiEffect
    data object NavigateBack : DetailUiEffect
    data class ShowSnackbar(val message: String) : DetailUiEffect
}
