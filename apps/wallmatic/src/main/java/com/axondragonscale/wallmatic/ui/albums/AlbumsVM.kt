package com.axondragonscale.wallmatic.ui.albums

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.axondragonscale.commons.collect
import com.axondragonscale.wallmatic.background.UpdateCoordinator
import com.axondragonscale.wallmatic.core.SyncManager
import com.axondragonscale.wallmatic.database.entity.Album
import com.axondragonscale.wallmatic.model.TargetScreen
import com.axondragonscale.wallmatic.repository.AppPrefsRepository
import com.axondragonscale.wallmatic.repository.WallmaticRepository
import com.axondragonscale.wallmatic.util.activateAlbum
import com.axondragonscale.wallmatic.util.activeTargetsFor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Created by Ronak Harkhani on 23/06/24
 */
class AlbumsVM(
    private val repository: WallmaticRepository,
    private val appPrefsRepository: AppPrefsRepository,
    private val syncManager: SyncManager,
    private val updateCoordinator: UpdateCoordinator,
): ViewModel() {

    val uiState = MutableStateFlow(AlbumsUiState())

    private val uiEffectChannel = Channel<AlbumsUiEffect>()
    val uiEffect = uiEffectChannel.receiveAsFlow().flowOn(Dispatchers.Main.immediate)

    init {
        viewModelScope.launch(Dispatchers.IO) {
            combine(
                repository.getAlbums(),
                appPrefsRepository.configFlow,
            ) { albums, config ->
                AlbumsSnapshot(
                    albums = albums,
                    activeTargets = albums
                        .associate { album -> album.id to config.activeTargetsFor(album.id) }
                        .filterValues { targets -> targets.isNotEmpty() },
                    mirrorEnabled = config.mirrorHomeConfigForLock,
                )
            }
                .distinctUntilChanged()
                .collect { snapshot ->
                    uiState.update {
                        it.copy(
                            isLoading = false,
                            albums = snapshot.albums,
                            activeTargets = snapshot.activeTargets,
                            mirrorEnabled = snapshot.mirrorEnabled,
                        )
                    }
                }
        }
    }

    fun onEvent(event: AlbumsUiEvent): Any = when (event) {
        is AlbumsUiEvent.CreateAlbum -> createAlbum(event)
        is AlbumsUiEvent.RenameAlbum -> renameAlbum(event)
        is AlbumsUiEvent.DeleteAlbum -> deleteAlbum(event)
        is AlbumsUiEvent.SetActive -> setActive(event)

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
        repository.deleteFullAlbum(event.albumId)
        // The deleted album may have been the configured one: repair rather than leave the tick
        // pointing at an album that no longer exists.
        syncManager.repairConfig()
        updateCoordinator.reschedule()
    }

    private fun setActive(event: AlbumsUiEvent.SetActive) = viewModelScope.launch(Dispatchers.IO) {
        appPrefsRepository.updateConfig { it.activateAlbum(event.albumId, event.target) }
        updateCoordinator.applyNow(event.target)
    }

}

private data class AlbumsSnapshot(
    val albums: List<Album>,
    val activeTargets: Map<Int, Set<TargetScreen>>,
    val mirrorEnabled: Boolean,
)
