package com.axondragonscale.qrqanvas.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.axondragonscale.qrqanvas.core.ThumbnailGenerator
import com.axondragonscale.qrqanvas.database.entity.QRCodeEntity
import com.axondragonscale.qrqanvas.repository.QRCodeRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Created by Ronak Harkhani on 09/07/25
 */
class HomeVM(
    private val qrCodeRepository: QRCodeRepository,
) : ViewModel() {

    companion object {
        private const val THUMBNAIL_SIZE = 200
    }

    val uiState = MutableStateFlow(HomeUiState())

    private val uiEffectChannel = Channel<HomeUiEffect>(Channel.BUFFERED)
    val uiEffect = uiEffectChannel.receiveAsFlow().flowOn(Dispatchers.Main.immediate)

    /** Tracks which entity IDs already have a thumbnail being generated or cached. */
    private val generatingIds = mutableSetOf<Long>()

    init {
        observeQRCodes()
    }

    fun onEvent(event: HomeUiEvent): Unit = when (event) {
        is HomeUiEvent.NavigateToCreator -> {
            viewModelScope.launch { uiEffectChannel.send(HomeUiEffect.NavigateToCreator) }
            Unit
        }
        is HomeUiEvent.NavigateToScanner -> {
            viewModelScope.launch { uiEffectChannel.send(HomeUiEffect.NavigateToScanner) }
            Unit
        }
        is HomeUiEvent.NavigateToHistory -> {
            viewModelScope.launch { uiEffectChannel.send(HomeUiEffect.NavigateToHistory) }
            Unit
        }
        is HomeUiEvent.NavigateToSettings -> {
            viewModelScope.launch { uiEffectChannel.send(HomeUiEffect.NavigateToSettings) }
            Unit
        }
        is HomeUiEvent.QRCodeClicked -> {
            viewModelScope.launch { uiEffectChannel.send(HomeUiEffect.NavigateToDetail(event.qrCodeId)) }
            Unit
        }
    }

    private fun observeQRCodes() = viewModelScope.launch {
        qrCodeRepository.getAll().collect { entities ->
            uiState.update { it.copy(qrCodes = entities) }
            generateMissingThumbnails(entities)
        }
    }

    /**
     * Generates thumbnail bitmaps for any QR codes that don't already have one cached.
     * Each thumbnail is rendered at a small size (~200px) for fast grid display.
     */
    private fun generateMissingThumbnails(entities: List<QRCodeEntity>) {
        val currentThumbnails = uiState.value.thumbnails
        val missing = entities.filter { it.id !in currentThumbnails && it.id !in generatingIds }
        if (missing.isEmpty()) return

        // Remove thumbnails for deleted entities
        val activeIds = entities.map { it.id }.toSet()
        if (currentThumbnails.keys.any { it !in activeIds }) {
            uiState.update { state ->
                state.copy(thumbnails = state.thumbnails.filterKeys { it in activeIds })
            }
        }

        for (entity in missing) {
            generatingIds.add(entity.id)
            viewModelScope.launch(Dispatchers.Default) {
                val bitmap = ThumbnailGenerator.generate(entity.content, entity.style)
                if (bitmap != null) {
                    uiState.update { state ->
                        state.copy(thumbnails = state.thumbnails + (entity.id to bitmap))
                    }
                }
                generatingIds.remove(entity.id)
            }
        }
    }
}
