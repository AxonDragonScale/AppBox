package com.axondragonscale.wallmatic.ui.library

/**
 * Created by Ronak Harkhani on 01/07/24
 */
sealed interface LibraryUiEffect {
    data class NavigateToAlbum(val albumId: Int): LibraryUiEffect
}
