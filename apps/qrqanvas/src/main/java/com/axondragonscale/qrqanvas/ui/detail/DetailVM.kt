package com.axondragonscale.qrqanvas.ui.detail

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil3.ImageLoader
import coil3.request.ImageRequest
import coil3.request.SuccessResult
import coil3.toBitmap
import com.axondragonscale.qrqanvas.core.QRExporter
import com.axondragonscale.qrqanvas.core.QRGenerator
import com.axondragonscale.qrqanvas.core.QRRenderer
import com.axondragonscale.qrqanvas.model.QRContent
import com.axondragonscale.qrqanvas.model.QRStyle
import com.axondragonscale.qrqanvas.repository.QRCodeRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

/**
 * Created by Ronak Harkhani on 09/07/25
 */
class DetailVM(
    private val application: Application,
    private val qrCodeRepository: QRCodeRepository,
    private val qrGenerator: QRGenerator,
    private val qrRenderer: QRRenderer,
    private val qrExporter: QRExporter,
) : ViewModel() {

    val uiState = MutableStateFlow(DetailUiState())

    private val _effects = Channel<DetailUiEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    private val json = Json { ignoreUnknownKeys = true }

    private var qrCodeId: Long = 0L
    private var logoBitmap: Bitmap? = null
    private var logoLoadJob: Job? = null

    /**
     * Loads the QR code entity by [id], parses its content and style,
     * and renders a full-size preview bitmap. Should be called once from
     * the composable when the screen first appears.
     */
    fun loadQRCode(id: Long) {
        qrCodeId = id
        viewModelScope.launch(Dispatchers.IO) {
            val entity = qrCodeRepository.getById(id)
            if (entity == null) {
                // Entity was deleted or doesn't exist — navigate back
                _effects.send(DetailUiEffect.NavigateBack)
                return@launch
            }

            val content: QRContent = json.decodeFromString(entity.contentJson)
            val style: QRStyle = json.decodeFromString(entity.styleJson)

            uiState.update {
                it.copy(
                    isLoading = false,
                    title = entity.title,
                    content = content,
                    createdAt = entity.createdAt,
                    modifiedAt = entity.modifiedAt,
                )
            }

            // Load logo bitmap if present, otherwise render immediately
            if (style.logoConfig != null) {
                loadLogoBitmapAndRender(style.logoConfig.uri, content, style)
            } else {
                renderQR(content, style, logoBitmap = null)
            }
        }
    }

    /**
     * Re-loads the entity from the database. Called when returning from the
     * editor so the detail screen reflects any changes made.
     */
    fun refresh() {
        if (qrCodeId != 0L) {
            logoBitmap = null
            loadQRCode(qrCodeId)
        }
    }

    fun onEvent(event: DetailUiEvent): Unit = when (event) {
        is DetailUiEvent.EditClicked -> onEditClicked()
        is DetailUiEvent.ShareClicked -> onShareClicked()
        is DetailUiEvent.ExportClicked -> onExportClicked()
        is DetailUiEvent.DeleteClicked -> onDeleteClicked()
        is DetailUiEvent.ConfirmDelete -> onConfirmDelete()
        is DetailUiEvent.DismissDelete -> onDismissDelete()
    }

    private fun onEditClicked() {
        viewModelScope.launch {
            _effects.send(DetailUiEffect.NavigateToEditor(qrCodeId))
        }
    }

    private fun onShareClicked() {
        val bitmap = uiState.value.qrBitmap ?: return
        val title = uiState.value.title
        viewModelScope.launch(Dispatchers.IO) {
            qrExporter.share(bitmap, title)
        }
    }

    private fun onExportClicked() {
        val bitmap = uiState.value.qrBitmap ?: return
        val title = uiState.value.title
        viewModelScope.launch(Dispatchers.IO) {
            val success = qrExporter.saveToDownloads(bitmap, title)
            val message = if (success) {
                "Saved to Downloads/QRQanvas"
            } else {
                "Failed to save QR code"
            }
            _effects.send(DetailUiEffect.ShowSnackbar(message))
        }
    }

    private fun onDeleteClicked() {
        uiState.update { it.copy(showDeleteDialog = true) }
    }

    private fun onConfirmDelete() {
        uiState.update { it.copy(showDeleteDialog = false) }
        viewModelScope.launch(Dispatchers.IO) {
            qrCodeRepository.delete(qrCodeId)
            _effects.send(DetailUiEffect.NavigateBack)
        }
    }

    private fun onDismissDelete() {
        uiState.update { it.copy(showDeleteDialog = false) }
    }

    /**
     * Loads the logo bitmap from [uri] using Coil, then renders the QR code.
     */
    private fun loadLogoBitmapAndRender(uri: String, content: QRContent, style: QRStyle) {
        logoLoadJob?.cancel()
        logoLoadJob = viewModelScope.launch(Dispatchers.IO) {
            val imageLoader = ImageLoader(application)
            val request = ImageRequest.Builder(application)
                .data(uri)
                .size(512)
                .build()

            val result = imageLoader.execute(request)
            if (result is SuccessResult) {
                logoBitmap = result.image.toBitmap()
            }
            renderQR(content, style, logoBitmap)
        }
    }

    /**
     * Renders the full-size QR code bitmap and updates the UI state.
     */
    private fun renderQR(content: QRContent, style: QRStyle, logoBitmap: Bitmap?) {
        val effectiveStyle = style.withEnforcedLogoErrorCorrection()
        val bitMatrix = qrGenerator.generate(content, effectiveStyle.errorCorrection)
        val bitmap = qrRenderer.render(bitMatrix, effectiveStyle, logoBitmap)
        uiState.update { it.copy(qrBitmap = bitmap) }
    }
}
