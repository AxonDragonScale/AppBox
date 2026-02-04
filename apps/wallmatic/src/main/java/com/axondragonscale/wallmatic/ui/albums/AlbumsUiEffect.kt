package com.axondragonscale.wallmatic.ui.albums

/**
 * Created by Ronak Harkhani on 01/07/24
 */
sealed interface AlbumsUiEffect {
    data class NavigateToAlbum(val albumId: Int): AlbumsUiEffect
}
