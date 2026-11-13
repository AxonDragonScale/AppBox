package com.axondragonscale.wallmatic.ui.wallpaper

/**
 * Created by Ronak Harkhani on 11/07/24
 */
sealed interface WallpaperUiEvent {
    data object EnterEdit : WallpaperUiEvent
    data object ExitEdit : WallpaperUiEvent
    data class ZoomChanged(val zoom: Float) : WallpaperUiEvent
    data class FocusChanged(val focalX: Float, val focalY: Float) : WallpaperUiEvent
    data object Center : WallpaperUiEvent
    data object Reset : WallpaperUiEvent
    data object Save : WallpaperUiEvent
    data object Back : WallpaperUiEvent
}
