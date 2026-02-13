package com.axondragonscale.qrqanvas.ui.detail

import android.graphics.Bitmap
import com.axondragonscale.qrqanvas.model.QRContent

/**
 * Created by Ronak Harkhani on 09/07/25
 */
data class DetailUiState(
    val isLoading: Boolean = true,
    val title: String = "",
    val content: QRContent? = null,
    val qrBitmap: Bitmap? = null,
    val createdAt: Long = 0L,
    val modifiedAt: Long = 0L,
    val showDeleteDialog: Boolean = false,
)
