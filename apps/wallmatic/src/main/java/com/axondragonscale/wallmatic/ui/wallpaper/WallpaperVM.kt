package com.axondragonscale.wallmatic.ui.wallpaper

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.axondragonscale.wallmatic.repository.WallmaticRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Created by Ronak Harkhani on 11/07/24
 */
class WallpaperVM(
    private val repository: WallmaticRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val wallpaperId = savedStateHandle.get<Int>("wallpaperId")!!
    val uiState = MutableStateFlow(WallpaperUiState())

    init {
        viewModelScope.launch(Dispatchers.IO) {
            uiState.update {
                it.copy(isLoading = false, wallpaper = repository.getWallpaper(wallpaperId))
            }
        }
    }

    fun onEvent(event: WallpaperUiEvent) = when (event) {

        else -> {}
    }
}
