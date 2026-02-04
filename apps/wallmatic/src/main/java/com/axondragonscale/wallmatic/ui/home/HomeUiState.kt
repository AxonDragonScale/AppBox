package com.axondragonscale.wallmatic.ui.home

import com.axondragonscale.wallmatic.database.entity.Album
import com.axondragonscale.wallmatic.database.entity.Wallpaper
import com.axondragonscale.wallmatic.model.Config
import com.axondragonscale.wallmatic.model.homeConfigOrNull
import com.axondragonscale.wallmatic.model.lockConfigOrNull

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
    val homeAlbum by lazy { albums.firstOrNull { it.id == config.homeConfigOrNull?.albumId } }
    val lockAlbum by lazy { albums.firstOrNull { it.id == config.lockConfigOrNull?.albumId } }
}
