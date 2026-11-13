package com.axondragonscale.wallmatic.ui.folder

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.axondragonscale.wallmatic.background.UpdateCoordinator
import com.axondragonscale.wallmatic.repository.AppPrefsRepository
import com.axondragonscale.wallmatic.repository.WallmaticRepository
import com.axondragonscale.wallmatic.ui.Route
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * Created by Ronak Harkhani on 17/09/24
 */
class FolderVM(
    private val savedStateHandle: SavedStateHandle,
    private val repository: WallmaticRepository,
    private val appPrefsRepository: AppPrefsRepository,
    private val updateCoordinator: UpdateCoordinator,
) : ViewModel() {

    private val folderId: Int = savedStateHandle.get<Int>(Route.Args.FOLDER_ID)!!
    private val wallpaperIds = MutableStateFlow<List<Int>>(emptyList())
    val uiState = MutableStateFlow(FolderUiState())

    init {
        viewModelScope.launch(Dispatchers.IO) {
            appPrefsRepository.gridSizeFlow.collect { gridSize ->
                uiState.value = uiState.value.copy(gridSize = gridSize)
            }
        }
        viewModelScope.launch(Dispatchers.IO) {
            syncUiStateWithFolder()
            observeWallpapers()
        }
    }

    fun onEvent(event: FolderUiEvent): Any = when (event) {
        is FolderUiEvent.BlacklistWallpaper -> onBlacklistWallpaper(event)
        is FolderUiEvent.WhitelistWallpaper -> onWhitelistWallpaper(event)
        is FolderUiEvent.SetWallpaper -> onSetWallpaper(event)

        // Handled by view
        is FolderUiEvent.NavigateToWallpaper -> Unit
        is FolderUiEvent.ShowWallpaperActions -> Unit
    }

    private suspend fun syncUiStateWithFolder() {
        val folder = repository.getFullFolder(folderId)
        uiState.value = uiState.value.copy(isLoading = false, folder = folder)
        wallpaperIds.value = folder?.wallpapers?.map { it.id }.orEmpty()
    }

    /** Keeps the grid's wallpapers live, so a crop saved elsewhere repaints without a reload. */
    private suspend fun observeWallpapers() = wallpaperIds.collectLatest { ids ->
        if (ids.isEmpty()) return@collectLatest
        repository.getWallpapersFlow(ids).collect { wallpapers ->
            uiState.value = uiState.value.copy(folder = uiState.value.folder?.copy(wallpapers = wallpapers))
        }
    }

    private inline fun runAndSyncState(
        crossinline block: suspend () -> Unit
    ) = viewModelScope.launch(Dispatchers.IO) {
        block()
        syncUiStateWithFolder()
    }

    private fun onBlacklistWallpaper(
        event: FolderUiEvent.BlacklistWallpaper,
    ) = runAndSyncState {
        repository.updateWallpaper(event.wallpaper.id) {
            isBlacklisted = true
        }
        // TODO: Update Cover
    }

    private fun onWhitelistWallpaper(
        event: FolderUiEvent.WhitelistWallpaper,
    ) = runAndSyncState {
        repository.updateWallpaper(event.wallpaper.id) {
            isBlacklisted = false
        }
        // TODO: Update Cover
    }

    private fun onSetWallpaper(
        event: FolderUiEvent.SetWallpaper,
    ) = viewModelScope.launch(Dispatchers.IO) {
        updateCoordinator.applyWallpaper(event.wallpaper, event.target)
    }

}
