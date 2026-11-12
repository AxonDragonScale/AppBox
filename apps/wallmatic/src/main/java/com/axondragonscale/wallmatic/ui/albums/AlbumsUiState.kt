package com.axondragonscale.wallmatic.ui.albums

import com.axondragonscale.wallmatic.database.entity.Album
import com.axondragonscale.wallmatic.model.TargetScreen

/**
 * Created by Ronak Harkhani on 23/06/24
 */
data class AlbumsUiState(
    val isLoading: Boolean = true,
    val albums: List<Album> = listOf(),
    val activeTargets: Map<Int, Set<TargetScreen>> = emptyMap(),
    val mirrorEnabled: Boolean = true,
) {
    /** The `HOME` / `LOCK` / `BOTH` badge an album carries, or null when it is not active. */
    fun targetLabelFor(albumId: Int): String? {
        val targets = activeTargets[albumId] ?: return null
        val home = TargetScreen.Home in targets
        val lock = TargetScreen.Lock in targets
        return when {
            home && lock -> "BOTH"
            mirrorEnabled && home -> "BOTH"
            home -> "HOME"
            lock -> "LOCK"
            else -> null
        }
    }
}
