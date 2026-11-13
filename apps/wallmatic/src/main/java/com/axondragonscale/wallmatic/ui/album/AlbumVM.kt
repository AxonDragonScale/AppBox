package com.axondragonscale.wallmatic.ui.album

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.axondragonscale.wallmatic.background.UpdateCoordinator
import com.axondragonscale.wallmatic.core.AlbumEditor
import com.axondragonscale.wallmatic.repository.ConfigRepository
import com.axondragonscale.wallmatic.repository.PreferencesRepository
import com.axondragonscale.wallmatic.repository.WallmaticRepository
import com.axondragonscale.wallmatic.ui.Route
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

/**
 * Created by Ronak Harkhani on 23/06/24
 */
internal class AlbumVM(
    private val savedStateHandle: SavedStateHandle,
    private val repository: WallmaticRepository,
    private val configRepository: ConfigRepository,
    private val preferencesRepository: PreferencesRepository,
    private val albumEditor: AlbumEditor,
    private val updateCoordinator: UpdateCoordinator,
) : ViewModel() {

    private val albumId: Int = savedStateHandle.get<Int>(Route.Args.ALBUM_ID)!!
    val uiState = MutableStateFlow(AlbumUiState())

    init {
        viewModelScope.launch(Dispatchers.IO) {
            combine(
                configRepository.configFlow,
                preferencesRepository.gridSize.flow,
                repository.observeFullAlbums(),
            ) { config, gridSize, albums ->
                AlbumUiState(
                    loading = false,
                    gridSize = gridSize,
                    album = albums.firstOrNull { it.id == albumId },
                    targetLabel = config.activeLabelFor(albumId),
                    isActive = config.activeTargetsFor(albumId).isNotEmpty(),
                    activatableTargets = config.activatableTargetsFor(albumId),
                )
            }.collect { uiState.value = it }
        }
    }

    fun onEvent(event: AlbumUiEvent): Any = when (event) {
        is AlbumUiEvent.FolderSelected -> onFolderSelected(event)
        is AlbumUiEvent.ImagesSelected -> onImagesSelected(event)
        is AlbumUiEvent.DeleteFolder -> onDeleteFolder(event)
        is AlbumUiEvent.SetWallpaper -> onSetWallpaper(event)
        is AlbumUiEvent.BlacklistWallpaper -> onBlacklistWallpaper(event)
        is AlbumUiEvent.WhitelistWallpaper -> onWhitelistWallpaper(event)
        is AlbumUiEvent.SetActive -> onSetActive(event)

        // Handled by View
        is AlbumUiEvent.NavigateToWallpaper -> Unit
        is AlbumUiEvent.NavigateToFolder -> Unit
        is AlbumUiEvent.ShowFolderActions -> Unit
        is AlbumUiEvent.ShowWallpaperActions -> Unit
    }

    private fun onFolderSelected(event: AlbumUiEvent.FolderSelected) = viewModelScope.launch(Dispatchers.IO) {
        albumEditor.addFolder(albumId, event.uri)
    }

    private fun onImagesSelected(event: AlbumUiEvent.ImagesSelected) = viewModelScope.launch(Dispatchers.IO) {
        albumEditor.addWallpapers(albumId, event.uris)
    }

    private fun onDeleteFolder(event: AlbumUiEvent.DeleteFolder) = viewModelScope.launch(Dispatchers.IO) {
        albumEditor.removeFolder(event.folderId)
        updateCoordinator.albumsChanged()
    }

    private fun onSetWallpaper(event: AlbumUiEvent.SetWallpaper) = viewModelScope.launch(Dispatchers.IO) {
        updateCoordinator.applyWallpaper(event.wallpaper, event.target)
    }

    private fun onBlacklistWallpaper(event: AlbumUiEvent.BlacklistWallpaper) =
        viewModelScope.launch(Dispatchers.IO) {
            repository.setBlacklisted(event.wallpaper.id, true)
        }

    private fun onWhitelistWallpaper(event: AlbumUiEvent.WhitelistWallpaper) =
        viewModelScope.launch(Dispatchers.IO) {
            repository.setBlacklisted(event.wallpaper.id, false)
        }

    private fun onSetActive(event: AlbumUiEvent.SetActive) = viewModelScope.launch(Dispatchers.IO) {
        configRepository.update { it.activate(albumId, event.target) }
        updateCoordinator.applyNow(event.target)
    }
}
