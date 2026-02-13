package com.axondragonscale.qrqanvas.ui.templates

/**
 * Created by Ronak Harkhani on 09/07/25
 */
sealed interface TemplatesUiEvent {
    data class RenameClicked(val templateId: String, val currentName: String) : TemplatesUiEvent
    data class RenameNameChanged(val name: String) : TemplatesUiEvent
    data object ConfirmRename : TemplatesUiEvent
    data object DismissRename : TemplatesUiEvent
    data class DeleteClicked(val templateId: String, val name: String) : TemplatesUiEvent
    data object ConfirmDelete : TemplatesUiEvent
    data object DismissDelete : TemplatesUiEvent
}
