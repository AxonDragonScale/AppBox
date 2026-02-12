package com.axondragonscale.wallmatic.ui.album

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.axondragonscale.wallmatic.core.AlbumManager
import com.axondragonscale.wallmatic.repository.AppPrefsRepository
import com.axondragonscale.wallmatic.repository.WallmaticRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
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
) : ViewModel() {

    private val albumId: Int = savedStateHandle.get<Int>("albumId")!!
    val uiState = MutableStateFlow(AlbumUiState())

    init {
        viewModelScope.launch(Dispatchers.IO) {
            appPrefsRepository.gridSizeFlow.collect { gridSize ->
                uiState.update { it.copy(gridSize = gridSize) }
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
        is AlbumUiEvent.DeleteWallpaper -> onDeleteWallpaper(event)

        // Handled by View
        is AlbumUiEvent.NavigateToWallpaper -> Unit
        is AlbumUiEvent.NavigateToFolder -> Unit
        is AlbumUiEvent.ShowFolderActions -> Unit
        is AlbumUiEvent.ShowWallpaperActions -> Unit
    }

    private suspend fun syncUiStateWithAlbum() {
        val album = repository.getFullAlbum(albumId)
        uiState.update { it.copy(loading = false, album = album) }
    }

    private inline fun runAndSyncState(
        crossinline block: suspend () -> Unit,
    ) = viewModelScope.launch(Dispatchers.IO) {
        block()
        syncUiStateWithAlbum()
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
        repository.deleteFolders(listOf(event.folderId))
    }

    private fun onDeleteWallpaper(
        event: AlbumUiEvent.DeleteWallpaper,
    ) = runAndSyncState {
        repository.deleteWallpapers(listOf(event.wallpaperId))
    }
}
