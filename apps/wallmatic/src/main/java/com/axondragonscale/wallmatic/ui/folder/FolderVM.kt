package com.axondragonscale.wallmatic.ui.folder

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.axondragonscale.wallmatic.background.UpdateCoordinator
import com.axondragonscale.wallmatic.repository.PreferencesRepository
import com.axondragonscale.wallmatic.repository.WallmaticRepository
import com.axondragonscale.wallmatic.ui.Route
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

/**
 * Created by Ronak Harkhani on 17/09/24
 */
class FolderVM(
    private val savedStateHandle: SavedStateHandle,
    private val repository: WallmaticRepository,
    private val preferencesRepository: PreferencesRepository,
    private val updateCoordinator: UpdateCoordinator,
) : ViewModel() {

    private val folderId: Int = savedStateHandle.get<Int>(Route.Args.FOLDER_ID)!!
    val uiState = MutableStateFlow(FolderUiState())

    init {
        viewModelScope.launch(Dispatchers.IO) {
            combine(
                preferencesRepository.gridSize.flow,
                repository.observeFullFolders(),
            ) { gridSize, folders ->
                FolderUiState(
                    isLoading = false,
                    gridSize = gridSize,
                    folder = folders.firstOrNull { it.id == folderId },
                )
            }.collect { uiState.value = it }
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

    private fun onBlacklistWallpaper(event: FolderUiEvent.BlacklistWallpaper) =
        viewModelScope.launch(Dispatchers.IO) {
            repository.setBlacklisted(event.wallpaper.id, true)
        }

    private fun onWhitelistWallpaper(event: FolderUiEvent.WhitelistWallpaper) =
        viewModelScope.launch(Dispatchers.IO) {
            repository.setBlacklisted(event.wallpaper.id, false)
        }

    private fun onSetWallpaper(event: FolderUiEvent.SetWallpaper) = viewModelScope.launch(Dispatchers.IO) {
        updateCoordinator.applyWallpaper(event.wallpaper, event.target)
    }

}
