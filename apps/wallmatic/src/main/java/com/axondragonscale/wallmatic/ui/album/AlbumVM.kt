package com.axondragonscale.wallmatic.ui.album

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.axondragonscale.wallmatic.background.UpdateCoordinator
import com.axondragonscale.wallmatic.core.AlbumManager
import com.axondragonscale.wallmatic.core.WallpaperUpdater
import com.axondragonscale.wallmatic.repository.AppPrefsRepository
import com.axondragonscale.wallmatic.repository.WallmaticRepository
import com.axondragonscale.wallmatic.ui.Route
import com.axondragonscale.wallmatic.util.activateAlbum
import com.axondragonscale.wallmatic.util.activeTargetsFor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Created by Ronak Harkhani on 23/06/24
 */
internal class AlbumVM(
    private val savedStateHandle: SavedStateHandle,
    private val repository: WallmaticRepository,
    private val appPrefsRepository: AppPrefsRepository,
    private val albumManager: AlbumManager,
    private val wallpaperUpdater: WallpaperUpdater,
    private val updateCoordinator: UpdateCoordinator,
) : ViewModel() {

    private val albumId: Int = savedStateHandle.get<Int>(Route.Args.ALBUM_ID)!!
    val uiState = MutableStateFlow(AlbumUiState())

    init {
        viewModelScope.launch(Dispatchers.IO) {
            appPrefsRepository.gridSizeFlow.collect { gridSize ->
                uiState.update { it.copy(gridSize = gridSize) }
            }
        }
        viewModelScope.launch(Dispatchers.IO) {
            appPrefsRepository.configFlow
                .map { config ->
                    config.activeTargetsFor(albumId) to config.mirrorHomeConfigForLock
                }
                .distinctUntilChanged()
                .collect { (targets, mirrorEnabled) ->
                    uiState.update {
                        it.copy(activeTargets = targets, mirrorEnabled = mirrorEnabled)
                    }
                }
        }
        viewModelScope.launch(Dispatchers.IO) {
            syncUiStateWithAlbum()
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

    private inline fun runAndSyncState(
        crossinline block: suspend () -> Unit,
    ) = viewModelScope.launch(Dispatchers.IO) {
        block()
        syncUiStateWithAlbum()
    }

    private suspend fun syncUiStateWithAlbum() {
        val album = repository.getFullAlbum(albumId)
        uiState.update { it.copy(loading = false, album = album) }
    }

    private fun onFolderSelected(
        event: AlbumUiEvent.FolderSelected,
    ) = runAndSyncState {
        val fullAlbum = uiState.value.album ?: return@runAndSyncState
        albumManager.forAlbum(fullAlbum) {
            addFolder(event.uri)
        }
    }

    private fun onImagesSelected(
        event: AlbumUiEvent.ImagesSelected,
    ) = runAndSyncState {
        val fullAlbum = uiState.value.album ?: return@runAndSyncState
        albumManager.forAlbum(fullAlbum) {
            addWallpapers(event.uris)
        }
    }

    private fun onDeleteFolder(
        event: AlbumUiEvent.DeleteFolder,
    ) = runAndSyncState {
        val fullAlbum = uiState.value.album ?: return@runAndSyncState
        albumManager.forAlbum(fullAlbum) {
            removeFolder(event.folderId)
        }
    }

    private fun onSetWallpaper(
        event: AlbumUiEvent.SetWallpaper,
    ) = viewModelScope.launch(Dispatchers.IO) {
        wallpaperUpdater.setWallpaper(event.wallpaper, event.target)
    }

    private fun onBlacklistWallpaper(
        event: AlbumUiEvent.BlacklistWallpaper,
    ) = runAndSyncState {
        repository.updateWallpaper(event.wallpaper.id) {
            isBlacklisted = true
        }
    }

    private fun onWhitelistWallpaper(
        event: AlbumUiEvent.WhitelistWallpaper,
    ) = runAndSyncState {
        repository.updateWallpaper(event.wallpaper.id) {
            isBlacklisted = false
        }
    }

    private fun onSetActive(event: AlbumUiEvent.SetActive) = viewModelScope.launch(Dispatchers.IO) {
        val id = albumId
        appPrefsRepository.updateConfig { config ->
            config.activateAlbum(id, event.target)
        }
        updateCoordinator.applyNow(event.target)
        syncUiStateWithAlbum()
    }
}
