package com.axondragonscale.qrqanvas.ui.templates

/**
 * Created by Ronak Harkhani on 09/07/25
 */
sealed interface TemplatesUiEffect {
    data class ShowSnackbar(val message: String) : TemplatesUiEffect
}
