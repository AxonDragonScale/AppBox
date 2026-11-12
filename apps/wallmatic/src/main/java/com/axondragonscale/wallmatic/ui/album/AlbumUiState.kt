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
    val activeTargets: Set<TargetScreen> = emptySet(),
    val mirrorEnabled: Boolean = true,
) {
    val isActive: Boolean get() = activeTargets.isNotEmpty()
    val hasContent: Boolean
        get() = album?.let { it.folders.isNotEmpty() || it.wallpapers.isNotEmpty() } == true
}
