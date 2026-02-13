package com.axondragonscale.qrqanvas.ui.home

import android.graphics.Bitmap
import com.axondragonscale.qrqanvas.database.entity.QRCodeEntity

/**
 * Created by Ronak Harkhani on 09/07/25
 */
data class HomeUiState(
    val qrCodes: List<QRCodeEntity> = emptyList(),
    val thumbnails: Map<Long, Bitmap> = emptyMap(),
)
