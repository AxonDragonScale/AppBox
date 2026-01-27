package com.axondragonscale.wallmatic.ui.albums

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.axondragonscale.wallmatic.database.entity.Album
import com.axondragonscale.wallmatic.repository.WallmaticRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Created by Ronak Harkhani on 23/06/24
 */
class AlbumsVM(
    private val repository: WallmaticRepository,
): ViewModel() {

    val uiState = MutableStateFlow(AlbumsUiState())

    private val uiEffectChannel = Channel<AlbumsUiEffect>()
    val uiEffect = uiEffectChannel.receiveAsFlow().flowOn(Dispatchers.Main.immediate)

    init {
        viewModelScope.launch(Dispatchers.IO) {
            repository.getAlbums().collect { albums ->
                uiState.update { it.copy(isLoading = false, albums = albums) }
            }
        }
    }

    fun onEvent(event: AlbumsUiEvent): Any = when (event) {
        is AlbumsUiEvent.CreateAlbum -> createAlbum(event)
        is AlbumsUiEvent.RenameAlbum -> renameAlbum(event)
        is AlbumsUiEvent.DeleteAlbum -> deleteAlbum(event)

        // Handled in the view
        is AlbumsUiEvent.ShowAlbumActionsDialog -> Unit
        is AlbumsUiEvent.ShowCreateAlbumDialog -> Unit
        is AlbumsUiEvent.NavigateToAlbum -> Unit
    }

    private fun createAlbum(event: AlbumsUiEvent.CreateAlbum) = viewModelScope.launch(Dispatchers.IO) {
        val albumId = repository.saveAlbum(Album(name = event.albumName))
        uiEffectChannel.send(AlbumsUiEffect.NavigateToAlbum(albumId))
    }

    private fun renameAlbum(event: AlbumsUiEvent.RenameAlbum) = viewModelScope.launch(Dispatchers.IO) {
        repository.updateAlbum(event.albumId) {
            name = event.albumName
        }
    }

    private fun deleteAlbum(event: AlbumsUiEvent.DeleteAlbum) = viewModelScope.launch(Dispatchers.IO) {
        repository.deleteAlbum(event.albumId)
    }

}
