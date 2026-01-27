package com.axondragonscale.wallmatic.ui.albums

import com.axondragonscale.wallmatic.database.entity.Album

/**
 * Created by Ronak Harkhani on 23/06/24
 */
sealed interface AlbumsUiEvent {
    data object ShowCreateAlbumDialog : AlbumsUiEvent
    data class ShowAlbumActionsDialog(val album: Album) : AlbumsUiEvent
    data class CreateAlbum(val albumName: String) : AlbumsUiEvent
    data class RenameAlbum(val albumId: Int, val albumName: String) : AlbumsUiEvent
    data class DeleteAlbum(val albumId: Int) : AlbumsUiEvent
    data class NavigateToAlbum(val albumId: Int): AlbumsUiEvent
}
