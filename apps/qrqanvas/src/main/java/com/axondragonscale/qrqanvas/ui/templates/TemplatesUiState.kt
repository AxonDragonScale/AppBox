package com.axondragonscale.qrqanvas.ui.templates

import com.axondragonscale.qrqanvas.model.Template

/**
 * Created by Ronak Harkhani on 09/07/25
 */
data class TemplatesUiState(
    val predefinedTemplates: List<Template> = emptyList(),
    val userTemplates: List<Template> = emptyList(),
    val templateThumbnails: Map<String, android.graphics.Bitmap> = emptyMap(),
    val showRenameDialog: Boolean = false,
    val renameTemplateId: String? = null,
    val renameTemplateName: String = "",
    val showDeleteDialog: Boolean = false,
    val deleteTemplateId: String? = null,
    val deleteTemplateName: String = "",
)
