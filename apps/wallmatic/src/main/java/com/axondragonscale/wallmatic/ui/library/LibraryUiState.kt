package com.axondragonscale.wallmatic.ui.library

import com.axondragonscale.wallmatic.model.FullAlbum
import com.axondragonscale.wallmatic.model.TargetScreen
import com.axondragonscale.wallmatic.model.WallmaticConfig

/**
 * Created by Ronak Harkhani on 23/06/24
 */
data class LibraryUiState(
    val isLoading: Boolean = true,
    val albums: List<FullAlbum> = emptyList(),
    val config: WallmaticConfig = WallmaticConfig(),
) {
    fun targetLabelFor(albumId: Int): String? = config.activeLabelFor(albumId)
    fun targetsFor(albumId: Int): List<TargetScreen> = config.activatableTargetsFor(albumId)
}
