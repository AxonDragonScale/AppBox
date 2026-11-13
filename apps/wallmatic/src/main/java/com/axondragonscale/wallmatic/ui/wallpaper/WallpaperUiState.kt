package com.axondragonscale.wallmatic.ui.wallpaper

import com.axondragonscale.wallmatic.database.entity.Wallpaper

/**
 * Created by Ronak Harkhani on 11/07/24
 */
data class WallpaperUiState(
    val isLoading: Boolean = true,
    val wallpaper: Wallpaper? = null,
    val isMissing: Boolean = false,
    val mode: WallpaperMode = WallpaperMode.View,
    val draftFocalX: Float = 0.5f,
    val draftFocalY: Float = 0.5f,
    val draftZoom: Float = 1f,
    val isSaving: Boolean = false,
) {
    /** True while the draft crop differs from the stored one. */
    val isDirty: Boolean
        get() = wallpaper != null && (
            draftFocalX != wallpaper.cropFocalX ||
                draftFocalY != wallpaper.cropFocalY ||
                draftZoom != wallpaper.cropZoom
            )
}

enum class WallpaperMode { View, Edit }
