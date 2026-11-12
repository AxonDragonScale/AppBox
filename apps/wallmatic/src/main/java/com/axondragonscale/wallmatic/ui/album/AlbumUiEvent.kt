package com.axondragonscale.wallmatic.ui.album

import android.net.Uri
import com.axondragonscale.wallmatic.database.entity.Wallpaper
import com.axondragonscale.wallmatic.model.FullFolder
import com.axondragonscale.wallmatic.model.TargetScreen

/**
 * Created by Ronak Harkhani on 23/06/24
 */
sealed interface AlbumUiEvent {
    data class FolderSelected(val uri: Uri) : AlbumUiEvent
    data class ImagesSelected(val uris: List<Uri>): AlbumUiEvent
    data class NavigateToFolder(val folderId: Int): AlbumUiEvent
    data class NavigateToWallpaper(val wallpaperId: Int): AlbumUiEvent
    data class ShowFolderActions(val folder: FullFolder): AlbumUiEvent
    data class ShowWallpaperActions(val wallpaper: Wallpaper): AlbumUiEvent
    data class DeleteFolder(val folderId: Int): AlbumUiEvent
    data class SetWallpaper(val wallpaper: Wallpaper, val target: TargetScreen): AlbumUiEvent
    data class BlacklistWallpaper(val wallpaper: Wallpaper) : AlbumUiEvent
    data class WhitelistWallpaper(val wallpaper: Wallpaper) : AlbumUiEvent
    data class SetActive(val target: TargetScreen) : AlbumUiEvent
}
