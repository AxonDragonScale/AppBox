package com.axondragonscale.wallmatic.ui.wallpaper

import com.axondragonscale.wallmatic.database.entity.Wallpaper

/**
 * Created by Ronak Harkhani on 11/07/24
 */
data class WallpaperUiState(
    val isLoading: Boolean = true,
    val wallpaper: Wallpaper? = null
)
