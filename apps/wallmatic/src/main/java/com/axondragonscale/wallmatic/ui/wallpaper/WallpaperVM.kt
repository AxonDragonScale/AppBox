package com.axondragonscale.wallmatic.ui.wallpaper

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.axondragonscale.wallmatic.background.UpdateCoordinator
import com.axondragonscale.wallmatic.core.MAX_CROP_ZOOM
import com.axondragonscale.wallmatic.core.MIN_CROP_ZOOM
import com.axondragonscale.wallmatic.repository.WallmaticRepository
import com.axondragonscale.wallmatic.ui.Route
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Created by Ronak Harkhani on 11/07/24
 */
class WallpaperVM(
    private val repository: WallmaticRepository,
    private val updateCoordinator: UpdateCoordinator,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val wallpaperId = savedStateHandle.get<Int>(Route.Args.WALLPAPER_ID)!!
    private val initialMode = if (savedStateHandle.get<Boolean>(Route.Args.EDIT) == true) {
        WallpaperMode.Edit
    } else {
        WallpaperMode.View
    }

    val uiState = MutableStateFlow(WallpaperUiState(mode = initialMode))

    init {
        viewModelScope.launch(Dispatchers.IO) {
            val wallpaper = repository.getWallpaper(wallpaperId)
            uiState.update {
                it.copy(
                    isLoading = false,
                    wallpaper = wallpaper,
                    isMissing = wallpaper == null,
                ).resetDraft(initialMode)
            }
        }
    }

    fun onEvent(event: WallpaperUiEvent) {
        when (event) {
            WallpaperUiEvent.EnterEdit -> uiState.update { it.resetDraft(WallpaperMode.Edit) }

            WallpaperUiEvent.ExitEdit, WallpaperUiEvent.Back -> uiState.update {
                it.resetDraft(WallpaperMode.View)
            }

            is WallpaperUiEvent.ZoomChanged -> uiState.update {
                it.copy(draftZoom = event.zoom.coerceIn(MIN_CROP_ZOOM, MAX_CROP_ZOOM))
            }

            is WallpaperUiEvent.FocusChanged -> uiState.update {
                it.copy(
                    draftFocalX = event.focalX.coerceIn(0f, 1f),
                    draftFocalY = event.focalY.coerceIn(0f, 1f),
                )
            }

            WallpaperUiEvent.Center -> uiState.update {
                it.copy(draftFocalX = 0.5f, draftFocalY = 0.5f)
            }

            WallpaperUiEvent.Reset -> uiState.update {
                it.copy(draftFocalX = 0.5f, draftFocalY = 0.5f, draftZoom = 1f)
            }

            WallpaperUiEvent.Save -> save()
        }
    }

    private fun save() = viewModelScope.launch(Dispatchers.IO) {
        val state = uiState.value
        val wallpaper = state.wallpaper ?: return@launch

        uiState.update { it.copy(isSaving = true) }
        repository.updateWallpaperCrop(
            wallpaperId = wallpaper.id,
            focalX = state.draftFocalX,
            focalY = state.draftFocalY,
            zoom = state.draftZoom,
        )
        val saved = repository.getWallpaper(wallpaperId) ?: return@launch
        updateCoordinator.reapply(saved)
        uiState.update {
            it.copy(wallpaper = saved, isSaving = false).resetDraft(WallpaperMode.View)
        }
    }
}

private fun WallpaperUiState.resetDraft(mode: WallpaperMode) = copy(
    mode = mode,
    draftFocalX = wallpaper?.cropFocalX ?: 0.5f,
    draftFocalY = wallpaper?.cropFocalY ?: 0.5f,
    draftZoom = wallpaper?.cropZoom ?: 1f,
)
