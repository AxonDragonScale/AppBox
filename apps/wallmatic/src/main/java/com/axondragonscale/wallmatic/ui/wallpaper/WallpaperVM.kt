package com.axondragonscale.wallmatic.ui.wallpaper

import android.content.Context
import android.graphics.BitmapFactory
import androidx.compose.ui.unit.IntSize
import androidx.core.net.toUri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.axondragonscale.wallmatic.database.entity.getCropRect
import com.axondragonscale.wallmatic.repository.WallmaticRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Created by Ronak Harkhani on 11/07/24
 */
class WallpaperVM(
    private val context: Context,
    private val repository: WallmaticRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val wallpaperId = savedStateHandle.get<Int>("wallpaperId")!!
    val uiState = MutableStateFlow(WallpaperUiState())

    init {
        viewModelScope.launch(Dispatchers.IO) {
            val wallpaper = repository.getWallpaper(wallpaperId)
            val imageSize = wallpaper?.let { getImageSize(it.uri) } ?: IntSize.Zero
            uiState.update {
                it.copy(
                    isLoading = false,
                    wallpaper = wallpaper,
                    imageSize = imageSize,
                )
            }
        }
    }

    private fun getImageSize(uri: String): IntSize {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri.toUri())
            val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeStream(inputStream, null, options)
            inputStream?.close()
            IntSize(options.outWidth, options.outHeight)
        } catch (e: Exception) {
            IntSize.Zero
        }
    }

    fun onEvent(event: WallpaperUiEvent) = when (event) {
        WallpaperUiEvent.ToggleEditMode -> {
            uiState.update { state ->
                state.copy(
                    isEditMode = !state.isEditMode,
                    tempCropRect = state.wallpaper?.getCropRect(),
                    tempRotation = state.wallpaper?.rotationDegrees ?: 0
                )
            }
        }

        is WallpaperUiEvent.UpdateCrop -> {
            uiState.update { it.copy(tempCropRect = event.rect) }
        }

        WallpaperUiEvent.RotateClockwise -> {
            uiState.update {
                it.copy(tempRotation = (it.tempRotation + 90) % 360)
            }
        }

        WallpaperUiEvent.RotateCounterClockwise -> {
            uiState.update {
                it.copy(tempRotation = (it.tempRotation - 90 + 360) % 360)
            }
        }

        WallpaperUiEvent.SaveEdits -> {
            viewModelScope.launch(Dispatchers.IO) {
                val state = uiState.value
                state.wallpaper?.let { wallpaper ->
                    val updatedWallpaper = wallpaper.copy(
                        cropLeft = state.tempCropRect?.left,
                        cropTop = state.tempCropRect?.top,
                        cropRight = state.tempCropRect?.right,
                        cropBottom = state.tempCropRect?.bottom,
                        rotationDegrees = state.tempRotation
                    )
                    repository.updateWallpaper(updatedWallpaper)
                    uiState.update {
                        it.copy(
                            isEditMode = false,
                            wallpaper = updatedWallpaper
                        )
                    }
                }
            }
        }

        WallpaperUiEvent.CancelEdits -> {
            uiState.update { it.copy(isEditMode = false) }
        }

        WallpaperUiEvent.ResetEdits -> {
            uiState.update {
                it.copy(tempCropRect = null, tempRotation = 0)
            }
        }
    }
}
