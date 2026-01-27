package com.axondragonscale.wallmatic.ui.album

import android.net.Uri
import com.axondragonscale.wallmatic.database.entity.Wallpaper
import com.axondragonscale.wallmatic.model.FullFolder

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
    data class DeleteWallpaper(val wallpaperId: Int): AlbumUiEvent
}
