package com.axondragonscale.qrqanvas.ui.detail

/**
 * Created by Ronak Harkhani on 09/07/25
 */
sealed interface DetailUiEvent {
    data object EditClicked : DetailUiEvent
    data object ShareClicked : DetailUiEvent
    data object ExportClicked : DetailUiEvent
    data object DeleteClicked : DetailUiEvent
    data object ConfirmDelete : DetailUiEvent
    data object DismissDelete : DetailUiEvent
}
