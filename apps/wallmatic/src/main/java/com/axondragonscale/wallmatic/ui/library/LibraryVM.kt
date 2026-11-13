package com.axondragonscale.wallmatic.ui.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.axondragonscale.wallmatic.background.UpdateCoordinator
import com.axondragonscale.wallmatic.repository.ConfigRepository
import com.axondragonscale.wallmatic.repository.WallmaticRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

/**
 * Created by Ronak Harkhani on 23/06/24
 */
class LibraryVM(
    private val repository: WallmaticRepository,
    private val configRepository: ConfigRepository,
    private val updateCoordinator: UpdateCoordinator,
): ViewModel() {

    val uiState = MutableStateFlow(LibraryUiState())

    private val uiEffectChannel = Channel<LibraryUiEffect>()
    val uiEffect = uiEffectChannel.receiveAsFlow().flowOn(Dispatchers.Main.immediate)

    init {
        viewModelScope.launch(Dispatchers.IO) {
            combine(
                repository.observeFullAlbums(),
                configRepository.configFlow,
            ) { albums, config ->
                LibraryUiState(
                    isLoading = false,
                    albums = albums,
                    config = config,
                )
            }
                .distinctUntilChanged()
                .collect { uiState.value = it }
        }
    }

    fun onEvent(event: LibraryUiEvent): Any = when (event) {
        is LibraryUiEvent.CreateAlbum -> createAlbum(event)
        is LibraryUiEvent.RenameAlbum -> renameAlbum(event)
        is LibraryUiEvent.DeleteAlbum -> deleteAlbum(event)
        is LibraryUiEvent.SetActive -> setActive(event)

        // Handled in the view
        is LibraryUiEvent.ShowAlbumActionsDialog -> Unit
        is LibraryUiEvent.ShowCreateAlbumDialog -> Unit
        is LibraryUiEvent.NavigateToAlbum -> Unit
    }

    private fun createAlbum(event: LibraryUiEvent.CreateAlbum) = viewModelScope.launch(Dispatchers.IO) {
        val albumId = repository.saveAlbum(event.albumName)
        uiEffectChannel.send(LibraryUiEffect.NavigateToAlbum(albumId))
    }

    private fun renameAlbum(event: LibraryUiEvent.RenameAlbum) = viewModelScope.launch(Dispatchers.IO) {
        repository.renameAlbum(event.albumId, event.albumName)
    }

    private fun deleteAlbum(event: LibraryUiEvent.DeleteAlbum) = viewModelScope.launch(Dispatchers.IO) {
        repository.deleteFullAlbum(event.albumId)
        updateCoordinator.albumsChanged()
    }

    private fun setActive(event: LibraryUiEvent.SetActive) = viewModelScope.launch(Dispatchers.IO) {
        configRepository.update { it.activate(event.albumId, event.target) }
        updateCoordinator.applyNow(event.target)
    }

}
