package com.axondragonscale.wallmatic.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.axondragonscale.wallmatic.background.UpdateCoordinator
import com.axondragonscale.wallmatic.model.Interval
import com.axondragonscale.wallmatic.model.TargetScreen
import com.axondragonscale.wallmatic.repository.ConfigRepository
import com.axondragonscale.wallmatic.repository.WallmaticRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlin.time.Duration

/**
 * Created by Ronak Harkhani on 22/06/24
 */
class HomeVM(
    private val repository: WallmaticRepository,
    private val configRepository: ConfigRepository,
    private val updateCoordinator: UpdateCoordinator,
) : ViewModel() {

    val uiState = MutableStateFlow(HomeUiState())

    init {
        viewModelScope.launch(Dispatchers.IO) {
            combine(
                configRepository.configFlow,
                repository.observeFullAlbums(),
            ) { config, albums ->
                val homeWallpaper = config.currentWallpaperIdFor(TargetScreen.Home)
                    ?.let { repository.getWallpaper(it) }
                val lockWallpaper = config.currentWallpaperIdFor(TargetScreen.Lock)
                    ?.let { repository.getWallpaper(it) }

                HomeUiState(
                    isLoading = false,
                    albums = albums,
                    config = config,
                    homeWallpaper = homeWallpaper,
                    lockWallpaper = lockWallpaper,
                )
            }.collect { uiState.value = it }
        }
    }

    fun onEvent(event: HomeUiEvent) = viewModelScope.launch(Dispatchers.IO) {
        when (event) {
            is HomeUiEvent.AlbumSelected -> onAlbumSelected(event)
            is HomeUiEvent.IntervalUpdated -> onIntervalUpdated(event.interval)
            is HomeUiEvent.RetryUpdate -> updateCoordinator.applyNow(event.target)
            is HomeUiEvent.ChangeNow -> updateCoordinator.applyNow(event.target)

            // Handled in Compose
            is HomeUiEvent.CreateAlbumClick -> Unit
            is HomeUiEvent.CustomIntervalClick -> Unit
            HomeUiEvent.SelectAlbumClick -> Unit
            is HomeUiEvent.NavigateToWallpaper -> Unit
        }
    }

    private suspend fun onAlbumSelected(event: HomeUiEvent.AlbumSelected) {
        configRepository.update { config ->
            if (!config.isInitialised) {
                config.firstRun(event.albumId)
            } else {
                config.activate(event.albumId, event.target)
            }
        }
        updateCoordinator.applyNow(event.target)
    }

    private suspend fun onIntervalUpdated(interval: Duration) {
        configRepository.setInterval(Interval.coerce(interval))
        updateCoordinator.reschedule()
    }
}
