package com.axondragonscale.wallmatic.ui.library

import com.axondragonscale.wallmatic.model.FullAlbum
import com.axondragonscale.wallmatic.model.TargetScreen

/**
 * Created by Ronak Harkhani on 23/06/24
 */
sealed interface LibraryUiEvent {
    data object ShowCreateAlbumDialog : LibraryUiEvent
    data class ShowAlbumActionsDialog(val album: FullAlbum) : LibraryUiEvent
    data class CreateAlbum(val albumName: String) : LibraryUiEvent
    data class RenameAlbum(val albumId: Int, val albumName: String) : LibraryUiEvent
    data class DeleteAlbum(val albumId: Int) : LibraryUiEvent
    data class SetActive(val albumId: Int, val target: TargetScreen) : LibraryUiEvent
    data class NavigateToAlbum(val albumId: Int): LibraryUiEvent
}
