package com.axondragonscale.qrqanvas.ui.home

/**
 * Created by Ronak Harkhani on 09/07/25
 */
sealed interface HomeUiEvent {
    data class QRCodeClicked(val qrCodeId: Long) : HomeUiEvent
}
