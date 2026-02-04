package com.axondragonscale.wallmatic.ui.folder

import com.axondragonscale.wallmatic.database.entity.Wallpaper

/**
 * Created by Ronak Harkhani on 17/09/24
 */
sealed interface FolderUiEvent {
    data class NavigateToWallpaper(val wallpaperId: Int) : FolderUiEvent
    data class ShowWallpaperActions(val wallpaper: Wallpaper) : FolderUiEvent
    data class BlacklistWallpaper(val wallpaper: Wallpaper) : FolderUiEvent
    data class WhitelistWallpaper(val wallpaper: Wallpaper) : FolderUiEvent
}
