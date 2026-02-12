package com.axondragonscale.wallmatic.ui.wallpaper

/**
 * Created by Ronak Harkhani on 11/07/24
 */
sealed interface WallpaperUiEvent {
    data object ToggleEditMode : WallpaperUiEvent
    data class UpdateCrop(val rect: android.graphics.Rect) : WallpaperUiEvent
    data object RotateClockwise : WallpaperUiEvent
    data object RotateCounterClockwise : WallpaperUiEvent
    data object SaveEdits : WallpaperUiEvent
    data object CancelEdits : WallpaperUiEvent
    data object ResetEdits : WallpaperUiEvent
}
