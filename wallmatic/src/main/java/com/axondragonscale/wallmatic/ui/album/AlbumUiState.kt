package com.axondragonscale.wallmatic.ui.album

import com.axondragonscale.wallmatic.model.FullAlbum

/**
 * Created by Ronak Harkhani on 23/06/24
 */
internal data class AlbumUiState(
    val loading: Boolean = true,
    val gridSize: Int = 2,
    val album: FullAlbum? = null,
)
