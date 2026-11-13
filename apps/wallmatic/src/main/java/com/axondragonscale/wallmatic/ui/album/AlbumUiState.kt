package com.axondragonscale.wallmatic.ui.album

import com.axondragonscale.wallmatic.model.FullAlbum
import com.axondragonscale.wallmatic.model.TargetScreen

/**
 * Created by Ronak Harkhani on 23/06/24
 */
internal data class AlbumUiState(
    val loading: Boolean = true,
    val gridSize: Int = 2,
    val album: FullAlbum? = null,
    val targetLabel: String? = null,
    val isActive: Boolean = false,
    val activatableTargets: List<TargetScreen> = emptyList(),
) {
    val hasContent: Boolean get() = album?.hasWallpapers == true
}
