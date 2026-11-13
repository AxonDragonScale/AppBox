package com.axondragonscale.wallmatic.ui.home

import com.axondragonscale.wallmatic.model.TargetScreen
import kotlin.time.Duration

/**
 * Created by Ronak Harkhani on 22/06/24
 */
sealed interface HomeUiEvent {
    data object CreateAlbumClick : HomeUiEvent
    data object CustomIntervalClick : HomeUiEvent
    data object SelectAlbumClick : HomeUiEvent
    data class AlbumSelected(val albumId: Int, val target: TargetScreen) : HomeUiEvent
    data class IntervalUpdated(val interval: Duration) : HomeUiEvent
    data class RetryUpdate(val target: TargetScreen) : HomeUiEvent
    data class ChangeNow(val target: TargetScreen) : HomeUiEvent
    data class NavigateToWallpaper(val wallpaperId: Int) : HomeUiEvent
}
