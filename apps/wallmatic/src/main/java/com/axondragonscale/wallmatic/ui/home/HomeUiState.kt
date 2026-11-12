package com.axondragonscale.wallmatic.ui.home

import com.axondragonscale.wallmatic.database.entity.Album
import com.axondragonscale.wallmatic.database.entity.Wallpaper
import com.axondragonscale.wallmatic.model.Config
import com.axondragonscale.wallmatic.model.TargetScreen
import com.axondragonscale.wallmatic.util.albumIdFor

/**
 * Created by Ronak Harkhani on 22/06/24
 */
data class HomeUiState(
    val isLoading: Boolean = true,
    val albums: List<Album> = emptyList(),
    val config: Config = Config.getDefaultInstance(),
    val homeWallpaper: Wallpaper? = null,
    val lockWallpaper: Wallpaper? = null,
) {
    fun albumFor(target: TargetScreen): Album? =
        albums.firstOrNull { it.id == config.albumIdFor(target) }

    fun wallpaperFor(target: TargetScreen): Wallpaper? = when (target) {
        TargetScreen.Home, TargetScreen.Both -> homeWallpaper
        TargetScreen.Lock -> lockWallpaper
    }
}
