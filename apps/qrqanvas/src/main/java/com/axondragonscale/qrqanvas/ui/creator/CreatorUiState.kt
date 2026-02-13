package com.axondragonscale.qrqanvas.ui.creator

import android.graphics.Bitmap
import com.axondragonscale.qrqanvas.model.QRStyle
import com.axondragonscale.qrqanvas.model.Template

/**
 * Created by Ronak Harkhani on 13/02/26
 */
data class CreatorUiState(
    val contentType: ContentType = ContentType.Text,
    val contentInput: ContentInputState = ContentInputState.Text(),
    val style: QRStyle = QRStyle(),
    val qrBitmap: Bitmap? = null,
    val isEditing: Boolean = false,
    val templates: List<Template> = emptyList(),
    val templateThumbnails: Map<String, Bitmap> = emptyMap(),
    val showSaveTemplateDialog: Boolean = false,
    val saveTemplateName: String = "",
)
