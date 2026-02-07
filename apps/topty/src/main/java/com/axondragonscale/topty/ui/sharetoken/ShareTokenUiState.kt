package com.axondragonscale.topty.ui.sharetoken

import android.graphics.Bitmap
import com.axondragonscale.topty.database.entity.Token

/**
 * Created by Ronak Harkhani on 28/12/24
 */
data class ShareTokenUiState(
    val isLoading: Boolean = true,
    val token: Token? = null,
    val qrBitmap: Bitmap? = null,
    val tokenUri: String = "",
    val error: String? = null,
)
