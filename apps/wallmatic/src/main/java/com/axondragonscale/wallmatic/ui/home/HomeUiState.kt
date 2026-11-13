package com.axondragonscale.wallmatic.ui.home

import com.axondragonscale.wallmatic.database.entity.Wallpaper
import com.axondragonscale.wallmatic.model.FullAlbum
import com.axondragonscale.wallmatic.model.TargetScreen
import com.axondragonscale.wallmatic.model.WallmaticConfig

/**
 * Created by Ronak Harkhani on 22/06/24
 */
data class HomeUiState(
    val isLoading: Boolean = true,
    val albums: List<FullAlbum> = emptyList(),
    val config: WallmaticConfig = WallmaticConfig(),
    val homeWallpaper: Wallpaper? = null,
    val lockWallpaper: Wallpaper? = null,
) {
    fun albumFor(target: TargetScreen): FullAlbum? =
        albums.firstOrNull { it.id == config.albumIdFor(target) }

    fun wallpaperFor(target: TargetScreen): Wallpaper? = when (target) {
        TargetScreen.Home, TargetScreen.Both -> homeWallpaper
        TargetScreen.Lock -> lockWallpaper
    }
}
