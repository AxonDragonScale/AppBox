package com.axondragonscale.wallmatic.ui.wallpaper

import com.axondragonscale.wallmatic.database.entity.Wallpaper
import com.axondragonscale.wallmatic.model.WallpaperCrop

/**
 * Created by Ronak Harkhani on 11/07/24
 */
data class WallpaperUiState(
    val isLoading: Boolean = true,
    val wallpaper: Wallpaper? = null,
    val mode: WallpaperMode = WallpaperMode.View,
    val draftCrop: WallpaperCrop = WallpaperCrop.Default,
    val isSaving: Boolean = false,
) {
    val isDirty: Boolean get() = wallpaper != null && draftCrop != wallpaper.crop
}

enum class WallpaperMode { View, Edit }
