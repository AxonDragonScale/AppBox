package com.axondragonscale.wallmatic.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.axondragonscale.commons.collect
import com.axondragonscale.wallmatic.background.UpdateCoordinator
import com.axondragonscale.wallmatic.model.Interval
import com.axondragonscale.wallmatic.model.copy
import com.axondragonscale.wallmatic.repository.AppPrefsRepository
import com.axondragonscale.wallmatic.repository.WallmaticRepository
import com.axondragonscale.wallmatic.util.activateAlbum
import com.axondragonscale.wallmatic.util.firstRun
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

/**
 * Created by Ronak Harkhani on 22/06/24
 */
class HomeVM(
    private val wallmaticRepository: WallmaticRepository,
    private val appPrefsRepository: AppPrefsRepository,
    private val updateCoordinator: UpdateCoordinator,
) : ViewModel() {

    val uiState = MutableStateFlow(HomeUiState())

    init {
        viewModelScope.launch(Dispatchers.IO) {
            combine(
                appPrefsRepository.configFlow,
                wallmaticRepository.getAlbums()
            ) { config, albums ->
                val homeWallpaper = config.homeConfig.currentWallpaperId
                    .takeIf { it != -1 }
                    ?.let { wallmaticRepository.getWallpaper(it) }

                val lockWallpaper = config.lockConfig.currentWallpaperId
                    .takeIf { it != -1 }
                    ?.let { wallmaticRepository.getWallpaper(it) }

                uiState.update {
                    it.copy(
                        isLoading = false,
                        config = config,
                        albums = albums,
                        homeWallpaper = homeWallpaper,
                        lockWallpaper = lockWallpaper
                    )
                }
            }.collect()
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
            is HomeUiEvent.SelectAlbumClick -> Unit
            is HomeUiEvent.NavigateToWallpaper -> Unit
        }
    }

    private suspend fun onAlbumSelected(event: HomeUiEvent.AlbumSelected) {
        appPrefsRepository.updateConfig { config ->
            if (!config.isInit) {
                config.firstRun(event.albumId)
            } else {
                config.activateAlbum(event.albumId, event.target)
            }
        }
        updateCoordinator.applyNow(event.target)
    }

    private suspend fun onIntervalUpdated(interval: Long) {
        if (appPrefsRepository.fastAutoCycleFlow.firstOrNull() == true) {
            appPrefsRepository.setFastAutoCycle(false)
        }
        val coercedInterval = Interval.coerce(interval.milliseconds).inWholeMilliseconds
        appPrefsRepository.updateConfig { it.copy { updateInterval = coercedInterval } }
        updateCoordinator.reschedule()
    }
}
