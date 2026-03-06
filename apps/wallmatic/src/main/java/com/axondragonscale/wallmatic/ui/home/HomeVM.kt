package com.axondragonscale.wallmatic.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.axondragonscale.commons.collect
import com.axondragonscale.wallmatic.background.WallpaperWorkScheduler
import com.axondragonscale.wallmatic.core.WallpaperUpdater
import com.axondragonscale.wallmatic.model.config
import com.axondragonscale.wallmatic.model.copy
import com.axondragonscale.wallmatic.model.wallpaperConfig
import com.axondragonscale.wallmatic.repository.AppPrefsRepository
import com.axondragonscale.wallmatic.repository.WallmaticRepository
import com.axondragonscale.wallmatic.util.homeConfig
import com.axondragonscale.wallmatic.util.lockConfig
import com.axondragonscale.wallmatic.util.nextUpdate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

/**
 * Created by Ronak Harkhani on 22/06/24
 */
class HomeVM(
    private val wallmaticRepository: WallmaticRepository,
    private val appPrefsRepository: AppPrefsRepository,
    private val scheduler: WallpaperWorkScheduler,
    private val wallpaperUpdater: WallpaperUpdater,
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
            is HomeUiEvent.MirrorHomeConfigForLockToggled -> onMirrorHomeConfigForLockToggled()
            is HomeUiEvent.AutoCycleToggled -> onAutoCycleToggled(event)
            is HomeUiEvent.IntervalUpdated -> onIntervalUpdated(event.interval)
            is HomeUiEvent.ChangeWallpaper -> wallpaperUpdater.updateWallpaper(event.target)

            // Handled in Compose
            is HomeUiEvent.CreateAlbumClick -> Unit
            is HomeUiEvent.SelectAlbumClick -> Unit
            is HomeUiEvent.NavigateToWallpaper -> Unit
        }
    }

    private suspend fun onAlbumSelected(event: HomeUiEvent.AlbumSelected) {
        appPrefsRepository.setConfig(
            if (!uiState.value.config.isInit) {
                // First time initialization
                config {
                    isInit = true
                    mirrorHomeConfigForLock = true
                    updateInterval = 15.minutes.inWholeMilliseconds
                    lastUpdated = -1
                    homeConfig = wallpaperConfig {
                        albumId = event.albumId
                        autoCycleEnabled = true
                        currentWallpaperId = -1 // Will be set after updateWallpaper
                    }
                    lockConfig = homeConfig
                }
            } else {
                uiState.value.config
                    .homeConfig { if (event.target.isHome()) albumId = event.albumId }
                    .lockConfig { if (event.target.isLock()) albumId = event.albumId }
            }
        )
        wallpaperUpdater.updateWallpaper(event.target)
        scheduler.scheduleNextUpdate()
    }

    private suspend fun onMirrorHomeConfigForLockToggled() {
        val updatedConfig = uiState.value.config.copy {
            lockConfig = homeConfig
            mirrorHomeConfigForLock = !mirrorHomeConfigForLock
        }
        appPrefsRepository.setConfig(updatedConfig)
        if (!updatedConfig.homeConfig.autoCycleEnabled && !updatedConfig.lockConfig.autoCycleEnabled)
            scheduler.cancelScheduledUpdates()
        else
            scheduler.scheduleNextUpdate()
    }

    private suspend fun onAutoCycleToggled(event: HomeUiEvent.AutoCycleToggled) {
        appPrefsRepository.setConfig(
            uiState.value.config
                .homeConfig { if (event.target.isHome()) autoCycleEnabled = event.enabled }
                .lockConfig { if (event.target.isLock()) autoCycleEnabled = event.enabled }
        )
        with(uiState.value.config) {
            if (!homeConfig.autoCycleEnabled && !lockConfig.autoCycleEnabled)
                scheduler.cancelScheduledUpdates()
            else if (homeConfig.autoCycleEnabled || lockConfig.autoCycleEnabled)
                scheduler.scheduleNextUpdate()
        }
    }

    private suspend fun onIntervalUpdated(interval: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            if (appPrefsRepository.fastAutoCycleFlow.firstOrNull() == true)
                appPrefsRepository.setFastAutoCycle(false)
        }
        val coercedInterval = interval.coerceAtMost(24.hours.inWholeMilliseconds)
        appPrefsRepository.setConfig(uiState.value.config.copy { updateInterval = coercedInterval })
        scheduler.scheduleNextUpdate()
    }
}
