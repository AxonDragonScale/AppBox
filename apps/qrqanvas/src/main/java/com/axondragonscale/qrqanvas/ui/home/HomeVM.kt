package com.axondragonscale.qrqanvas.ui.home

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.axondragonscale.qrqanvas.core.QRGenerator
import com.axondragonscale.qrqanvas.core.QRRenderer
import com.axondragonscale.qrqanvas.database.entity.QRCodeEntity
import com.axondragonscale.qrqanvas.model.QRContent
import com.axondragonscale.qrqanvas.model.QRStyle
import com.axondragonscale.qrqanvas.repository.QRCodeRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

/**
 * Created by Ronak Harkhani on 09/07/25
 */
class HomeVM(
    private val qrCodeRepository: QRCodeRepository,
    private val qrGenerator: QRGenerator,
    private val qrRenderer: QRRenderer,
) : ViewModel() {

    val uiState = MutableStateFlow(HomeUiState())

    private val json = Json { ignoreUnknownKeys = true }

    /** Tracks which entity IDs already have a thumbnail being generated or cached. */
    private val generatingIds = mutableSetOf<Long>()

    init {
        observeQRCodes()
    }

    fun onEvent(event: HomeUiEvent): Unit = when (event) {
        is HomeUiEvent.QRCodeClicked -> Unit // Handled by navigation callback in composable
    }

    private fun observeQRCodes() {
        viewModelScope.launch {
            qrCodeRepository.getAll().collect { entities ->
                uiState.update { it.copy(qrCodes = entities) }
                generateMissingThumbnails(entities)
            }
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
                val bitmap = renderThumbnail(entity)
                if (bitmap != null) {
                    uiState.update { state ->
                        state.copy(thumbnails = state.thumbnails + (entity.id to bitmap))
                    }
                }
                generatingIds.remove(entity.id)
            }
        }
    }

    /**
     * Renders a small QR code thumbnail from the entity's serialized content and style.
     */
    private fun renderThumbnail(entity: QRCodeEntity): Bitmap? {
        return try {
            val content: QRContent = json.decodeFromString(entity.contentJson)
            val style: QRStyle = json.decodeFromString(entity.styleJson)
            val thumbnailStyle = style.copy(size = THUMBNAIL_SIZE, logoConfig = null)
            val bitMatrix = qrGenerator.generate(content, thumbnailStyle.errorCorrection)
            qrRenderer.render(bitMatrix, thumbnailStyle)
        } catch (e: Exception) {
            null
        }
    }

    companion object {
        private const val THUMBNAIL_SIZE = 200
    }
}
