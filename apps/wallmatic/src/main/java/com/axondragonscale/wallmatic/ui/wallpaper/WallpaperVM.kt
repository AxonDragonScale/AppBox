package com.axondragonscale.wallmatic.ui.wallpaper

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.axondragonscale.wallmatic.background.UpdateCoordinator
import com.axondragonscale.wallmatic.core.MAX_CROP_ZOOM
import com.axondragonscale.wallmatic.core.MIN_CROP_ZOOM
import com.axondragonscale.wallmatic.model.WallpaperCrop
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
                ).resetDraft(initialMode)
            }
        }
    }

    fun onEvent(event: WallpaperUiEvent): Any = when (event) {
        WallpaperUiEvent.EnterEdit -> uiState.update { it.resetDraft(WallpaperMode.Edit) }

        WallpaperUiEvent.Back -> uiState.update {
            it.resetDraft(WallpaperMode.View)
        }

        is WallpaperUiEvent.ZoomChanged -> uiState.update {
            it.copy(
                draftCrop = it.draftCrop.copy(
                    zoom = event.zoom.coerceIn(MIN_CROP_ZOOM, MAX_CROP_ZOOM),
                ),
            )
        }

        is WallpaperUiEvent.FocusChanged -> uiState.update {
            it.copy(
                draftCrop = it.draftCrop.copy(
                    focalX = event.focalX.coerceIn(0f, 1f),
                    focalY = event.focalY.coerceIn(0f, 1f),
                ),
            )
        }

        WallpaperUiEvent.Center -> uiState.update {
            it.copy(draftCrop = it.draftCrop.copy(focalX = 0.5f, focalY = 0.5f))
        }

        WallpaperUiEvent.Reset -> uiState.update {
            it.copy(draftCrop = WallpaperCrop.Default)
        }

        WallpaperUiEvent.Save -> save()
    }

    private fun save() = viewModelScope.launch(Dispatchers.IO) {
        val state = uiState.value
        val wallpaper = state.wallpaper ?: return@launch

        uiState.update { it.copy(isSaving = true) }
        repository.updateWallpaperCrop(wallpaper.id, state.draftCrop)
        // id lives outside the constructor, so copy() would drop it and reapply would not match.
        val saved = wallpaper.copy(crop = state.draftCrop).apply { id = wallpaper.id }
        updateCoordinator.reapply(saved)
        uiState.update {
            it.copy(wallpaper = saved, isSaving = false).resetDraft(WallpaperMode.View)
        }
    }
}

private fun WallpaperUiState.resetDraft(mode: WallpaperMode) = copy(
    mode = mode,
    draftCrop = wallpaper?.crop ?: WallpaperCrop.Default,
)
