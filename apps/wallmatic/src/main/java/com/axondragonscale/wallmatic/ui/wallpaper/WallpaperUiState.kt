package com.axondragonscale.wallmatic.ui.wallpaper

import androidx.compose.ui.unit.IntSize
import com.axondragonscale.wallmatic.database.entity.Wallpaper

/**
 * Created by Ronak Harkhani on 11/07/24
 */
data class WallpaperUiState(
    val isLoading: Boolean = true,
    val wallpaper: Wallpaper? = null,
    val imageSize: IntSize = IntSize.Zero,
    val isEditMode: Boolean = false,
    val tempCropRect: android.graphics.Rect? = null,
    val tempRotation: Int = 0,
)
