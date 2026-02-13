package com.axondragonscale.qrqanvas.ui.creator

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
import com.axondragonscale.qrqanvas.database.entity.QRCodeEntity
import com.axondragonscale.qrqanvas.model.ColorConfig
import com.axondragonscale.qrqanvas.model.DotStyle
import com.axondragonscale.qrqanvas.model.ErrorCorrectionLevel
import com.axondragonscale.qrqanvas.model.EyeStyle
import com.axondragonscale.qrqanvas.model.LogoConfig
import com.axondragonscale.qrqanvas.model.LogoShape
import com.axondragonscale.qrqanvas.model.PredefinedTemplates
import com.axondragonscale.qrqanvas.model.QRContent
import com.axondragonscale.qrqanvas.model.QRStyle
import com.axondragonscale.qrqanvas.model.Template
import com.axondragonscale.qrqanvas.repository.QRCodeRepository
import com.axondragonscale.qrqanvas.repository.TemplateRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Created by Ronak Harkhani on 13/02/26
 */
class CreatorVM(
    private val application: Application,
    private val qrGenerator: QRGenerator,
    private val qrRenderer: QRRenderer,
    private val qrCodeRepository: QRCodeRepository,
    private val qrExporter: QRExporter,
    private val templateRepository: TemplateRepository,
) : ViewModel() {

    val uiState = MutableStateFlow(CreatorUiState())

    private val _effects = Channel<CreatorUiEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    private var renderJob: Job? = null
    private var logoLoadJob: Job? = null

    /** Cached logo bitmap loaded from the URI in [LogoConfig]. */
    private var logoBitmap: Bitmap? = null

    /** The ID of the QR code being edited, or null if creating a new one. */
    private var editingId: Long? = null

    private val json = Json { ignoreUnknownKeys = true }

    init {
        observeTemplates()
    }

    /**
     * Loads an existing QR code for editing. Should be called once from the
     * composable when a non-null [qrCodeId] route argument is provided.
     */
    fun loadQRCode(qrCodeId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            val entity = qrCodeRepository.getById(qrCodeId) ?: return@launch
            editingId = entity.id

            val content: QRContent = json.decodeFromString(entity.contentJson)
            val style: QRStyle = json.decodeFromString(entity.styleJson)

            val contentType = content.toContentType()
            val contentInput = content.toContentInputState()

            uiState.update {
                it.copy(
                    contentType = contentType,
                    contentInput = contentInput,
                    style = style,
                    isEditing = true,
                )
            }

            // Load logo bitmap if the style has one
            style.logoConfig?.let { loadLogoBitmap(it.uri) } ?: regenerateQR()
        }
    }

    fun onEvent(event: CreatorUiEvent): Unit = when (event) {
        is CreatorUiEvent.ContentTypeSelected -> onContentTypeSelected(event.contentType)
        is CreatorUiEvent.ContentInputChanged -> onContentInputChanged(event.contentInput)
        is CreatorUiEvent.DotStyleSelected -> onDotStyleSelected(event.dotStyle)
        is CreatorUiEvent.DotColorSelected -> onDotColorSelected(event.color)
        is CreatorUiEvent.EyeStyleSelected -> onEyeStyleSelected(event.eyeStyle)
        is CreatorUiEvent.EyeFrameColorSelected -> onEyeFrameColorSelected(event.color)
        is CreatorUiEvent.EyeDotColorSelected -> onEyeDotColorSelected(event.color)
        is CreatorUiEvent.BackgroundColorSelected -> onBackgroundColorSelected(event.color)
        is CreatorUiEvent.LogoSelected -> onLogoSelected(event.uri)
        is CreatorUiEvent.LogoSizeChanged -> onLogoSizeChanged(event.size)
        is CreatorUiEvent.LogoPaddingChanged -> onLogoPaddingChanged(event.padding)
        is CreatorUiEvent.LogoShapeSelected -> onLogoShapeSelected(event.shape)
        is CreatorUiEvent.ErrorCorrectionSelected -> onErrorCorrectionSelected(event.level)
        is CreatorUiEvent.QuietZoneChanged -> onQuietZoneChanged(event.quietZone)
        is CreatorUiEvent.TemplateSelected -> onTemplateSelected(event.style)
        is CreatorUiEvent.SaveClicked -> onSaveClicked()
        is CreatorUiEvent.ShareClicked -> onShareClicked()
        is CreatorUiEvent.ExportClicked -> onExportClicked()
        is CreatorUiEvent.SaveAsTemplateClicked -> onSaveAsTemplateClicked()
        is CreatorUiEvent.SaveTemplateNameChanged -> onSaveTemplateNameChanged(event.name)
        is CreatorUiEvent.ConfirmSaveTemplate -> onConfirmSaveTemplate()
        is CreatorUiEvent.DismissSaveTemplate -> onDismissSaveTemplate()
    }

    private fun onContentTypeSelected(contentType: ContentType) {
        if (contentType == uiState.value.contentType) return
        val newInput = contentType.toDefaultInput()
        uiState.update {
            it.copy(
                contentType = contentType,
                contentInput = newInput,
                qrBitmap = null,
            )
        }
    }

    private fun onContentInputChanged(contentInput: ContentInputState) {
        uiState.update { it.copy(contentInput = contentInput) }
        regenerateQR()
    }

    private fun onDotStyleSelected(dotStyle: DotStyle) {
        if (dotStyle == uiState.value.style.dotStyle) return
        uiState.update { it.copy(style = it.style.copy(dotStyle = dotStyle)) }
        regenerateQR()
    }

    private fun onDotColorSelected(color: ColorConfig) {
        if (color == uiState.value.style.dotColor) return
        uiState.update { it.copy(style = it.style.copy(dotColor = color)) }
        regenerateQR()
    }

    private fun onEyeStyleSelected(eyeStyle: EyeStyle) {
        if (eyeStyle == uiState.value.style.eyeStyle) return
        uiState.update { it.copy(style = it.style.copy(eyeStyle = eyeStyle)) }
        regenerateQR()
    }

    private fun onEyeFrameColorSelected(color: ColorConfig) {
        if (color == uiState.value.style.eyeFrameColor) return
        uiState.update { it.copy(style = it.style.copy(eyeFrameColor = color)) }
        regenerateQR()
    }

    private fun onEyeDotColorSelected(color: ColorConfig) {
        if (color == uiState.value.style.eyeDotColor) return
        uiState.update { it.copy(style = it.style.copy(eyeDotColor = color)) }
        regenerateQR()
    }

    private fun onBackgroundColorSelected(color: ColorConfig) {
        if (color == uiState.value.style.backgroundColor) return
        uiState.update { it.copy(style = it.style.copy(backgroundColor = color)) }
        regenerateQR()
    }

    private fun onLogoSelected(uri: String?) {
        if (uri == null) {
            // Remove logo
            logoBitmap = null
            uiState.update { it.copy(style = it.style.copy(logoConfig = null)) }
            regenerateQR()
            return
        }

        // Set logo config (create new or update URI on existing)
        val currentConfig = uiState.value.style.logoConfig
        val newConfig = currentConfig?.copy(uri = uri) ?: LogoConfig(uri = uri)
        uiState.update { it.copy(style = it.style.copy(logoConfig = newConfig)) }

        // Load the logo bitmap asynchronously
        loadLogoBitmap(uri)
    }

    private fun onLogoSizeChanged(size: Float) {
        val config = uiState.value.style.logoConfig ?: return
        if (size == config.size) return
        uiState.update {
            it.copy(style = it.style.copy(logoConfig = config.copy(size = size)))
        }
        regenerateQR()
    }

    private fun onLogoPaddingChanged(padding: Float) {
        val config = uiState.value.style.logoConfig ?: return
        if (padding == config.padding) return
        uiState.update {
            it.copy(style = it.style.copy(logoConfig = config.copy(padding = padding)))
        }
        regenerateQR()
    }

    private fun onLogoShapeSelected(shape: LogoShape) {
        val config = uiState.value.style.logoConfig ?: return
        if (shape == config.shape) return
        uiState.update {
            it.copy(style = it.style.copy(logoConfig = config.copy(shape = shape)))
        }
        regenerateQR()
    }

    private fun onErrorCorrectionSelected(level: ErrorCorrectionLevel) {
        if (level == uiState.value.style.errorCorrection) return
        uiState.update { it.copy(style = it.style.copy(errorCorrection = level)) }
        regenerateQR()
    }

    private fun onQuietZoneChanged(quietZone: Int) {
        if (quietZone == uiState.value.style.quietZone) return
        uiState.update { it.copy(style = it.style.copy(quietZone = quietZone)) }
        regenerateQR()
    }

    private fun onTemplateSelected(style: QRStyle) {
        // Apply the template style, preserving the current output size
        logoBitmap = null
        uiState.update {
            it.copy(style = style.copy(size = it.style.size))
        }
        regenerateQR()
    }

    private fun onSaveClicked() {
        val state = uiState.value
        val content = state.contentInput.toQRContent() ?: return

        val contentJson = json.encodeToString(content)
        val styleJson = json.encodeToString(state.style)
        val title = generateTitle(content)
        val now = System.currentTimeMillis()

        viewModelScope.launch(Dispatchers.IO) {
            val existingId = editingId
            if (existingId != null) {
                // Update existing entity
                val existing = qrCodeRepository.getById(existingId) ?: return@launch
                qrCodeRepository.update(
                    existing.copy(
                        title = title,
                        contentJson = contentJson,
                        styleJson = styleJson,
                        modifiedAt = now,
                    )
                )
            } else {
                // Insert new entity
                qrCodeRepository.insert(
                    QRCodeEntity(
                        title = title,
                        contentJson = contentJson,
                        styleJson = styleJson,
                        createdAt = now,
                        modifiedAt = now,
                    )
                )
            }
            _effects.send(CreatorUiEffect.SaveCompleted)
        }
    }

    private fun onShareClicked() {
        val bitmap = uiState.value.qrBitmap ?: return
        viewModelScope.launch(Dispatchers.IO) {
            val title = uiState.value.contentInput.toQRContent()
                ?.let { generateTitle(it) } ?: "qr_code"
            qrExporter.share(bitmap, title)
        }
    }

    private fun onExportClicked() {
        val bitmap = uiState.value.qrBitmap ?: return
        viewModelScope.launch(Dispatchers.IO) {
            val title = uiState.value.contentInput.toQRContent()
                ?.let { generateTitle(it) } ?: "qr_code"
            val success = qrExporter.saveToDownloads(bitmap, title)
            val message = if (success) {
                "Saved to Downloads/QRQanvas"
            } else {
                "Failed to save QR code"
            }
            _effects.send(CreatorUiEffect.ShowSnackbar(message))
        }
    }

    /**
     * Auto-generates a human-readable title from the QR content.
     */
    private fun generateTitle(content: QRContent): String = when (content) {
        is QRContent.Text -> content.value.take(50)
        is QRContent.Url -> content.url.take(50)
        is QRContent.WiFi -> "Wi-Fi: ${content.ssid}"
        is QRContent.Email -> "Email: ${content.address}"
        is QRContent.Phone -> "Phone: ${content.number}"
        is QRContent.Sms -> "SMS: ${content.number}"
        is QRContent.Contact -> "Contact: ${content.name}"
        is QRContent.Location -> "Location: ${content.latitude}, ${content.longitude}"
    }

    /**
     * Observes the unified template list (predefined + user) from the repository
     * and generates thumbnails for any templates that don't already have one.
     */
    private fun observeTemplates() {
        viewModelScope.launch {
            templateRepository.getAll().collect { templates ->
                uiState.update { it.copy(templates = templates) }
                generateMissingThumbnails(templates)
            }
        }
    }

    /**
     * Generates QR code thumbnails for templates that don't already have one cached.
     * Uses a dummy content string and a small output size for fast rendering.
     */
    private fun generateMissingThumbnails(templates: List<Template>) {
        val currentThumbnails = uiState.value.templateThumbnails
        val missing = templates.filter { it.id !in currentThumbnails }
        if (missing.isEmpty()) return

        viewModelScope.launch(Dispatchers.Default) {
            val dummyContent = QRContent.Text("QRQanvas")
            val thumbnailSize = 200

            val newThumbnails = missing.associate { template ->
                val style = template.style.copy(size = thumbnailSize, quietZone = 1)
                val bitMatrix = qrGenerator.generate(dummyContent, style.errorCorrection)
                val bitmap = qrRenderer.render(bitMatrix, style)
                template.id to bitmap
            }

            uiState.update { it.copy(templateThumbnails = it.templateThumbnails + newThumbnails) }
        }
    }

    private fun onSaveAsTemplateClicked() {
        uiState.update {
            it.copy(showSaveTemplateDialog = true, saveTemplateName = "")
        }
    }

    private fun onSaveTemplateNameChanged(name: String) {
        uiState.update { it.copy(saveTemplateName = name) }
    }

    private fun onConfirmSaveTemplate() {
        val name = uiState.value.saveTemplateName.trim()
        if (name.isEmpty()) return

        val style = uiState.value.style
        uiState.update { it.copy(showSaveTemplateDialog = false, saveTemplateName = "") }

        viewModelScope.launch(Dispatchers.IO) {
            // Strip logo and size before saving as template — templates are style-only
            val templateStyle = style.copy(logoConfig = null, size = 1024)
            templateRepository.insert(name, templateStyle)
            _effects.send(CreatorUiEffect.ShowSnackbar("Template \"$name\" saved"))
        }
    }

    private fun onDismissSaveTemplate() {
        uiState.update { it.copy(showSaveTemplateDialog = false, saveTemplateName = "") }
    }

    /**
     * Loads the logo bitmap from the given [uri] using Coil.
     * Once loaded, caches it and triggers a QR re-render.
     */
    private fun loadLogoBitmap(uri: String) {
        logoLoadJob?.cancel()
        logoLoadJob = viewModelScope.launch(Dispatchers.IO) {
            val imageLoader = ImageLoader(application)
            val request = ImageRequest.Builder(application)
                .data(uri)
                .size(512) // Reasonable size for logo overlay
                .build()

            val result = imageLoader.execute(request)
            if (result is SuccessResult) {
                logoBitmap = result.image.toBitmap()
                regenerateQR()
            }
        }
    }

    private fun regenerateQR() {
        renderJob?.cancel()

        val state = uiState.value
        val content = state.contentInput.toQRContent()
        if (content == null) {
            uiState.update { it.copy(qrBitmap = null) }
            return
        }

        // Auto-enforce High error correction when a logo is present
        val style = state.style.withEnforcedLogoErrorCorrection()

        renderJob = viewModelScope.launch(Dispatchers.Default) {
            val bitMatrix = qrGenerator.generate(content, style.errorCorrection)
            val bitmap = qrRenderer.render(bitMatrix, style, logoBitmap)
            uiState.update { it.copy(qrBitmap = bitmap) }
        }
    }

    private fun ContentType.toDefaultInput(): ContentInputState = when (this) {
        ContentType.Text -> ContentInputState.Text()
        ContentType.Url -> ContentInputState.Url()
        ContentType.WiFi -> ContentInputState.WiFi()
        ContentType.Email -> ContentInputState.Email()
        ContentType.Phone -> ContentInputState.Phone()
        ContentType.Sms -> ContentInputState.Sms()
    }

    private fun ContentInputState.toQRContent(): QRContent? = when (this) {
        is ContentInputState.Text ->
            if (isValid) QRContent.Text(value) else null

        is ContentInputState.Url ->
            if (isValid) QRContent.Url(url) else null

        is ContentInputState.WiFi ->
            if (isValid) QRContent.WiFi(ssid, password, encryption) else null

        is ContentInputState.Email ->
            if (isValid) QRContent.Email(
                address = address,
                subject = subject.ifBlank { null },
                body = body.ifBlank { null },
            ) else null

        is ContentInputState.Phone ->
            if (isValid) QRContent.Phone(number) else null

        is ContentInputState.Sms ->
            if (isValid) QRContent.Sms(
                number = number,
                message = message.ifBlank { null },
            ) else null
    }

    /**
     * Maps a [QRContent] back to the corresponding [ContentType] for the UI selector.
     */
    private fun QRContent.toContentType(): ContentType = when (this) {
        is QRContent.Text -> ContentType.Text
        is QRContent.Url -> ContentType.Url
        is QRContent.WiFi -> ContentType.WiFi
        is QRContent.Email -> ContentType.Email
        is QRContent.Phone -> ContentType.Phone
        is QRContent.Sms -> ContentType.Sms
        is QRContent.Contact -> ContentType.Text // fallback — Contact not in creator types
        is QRContent.Location -> ContentType.Text // fallback — Location not in creator types
    }

    /**
     * Maps a [QRContent] back to the corresponding [ContentInputState] to pre-fill the form.
     */
    private fun QRContent.toContentInputState(): ContentInputState = when (this) {
        is QRContent.Text -> ContentInputState.Text(value = value)
        is QRContent.Url -> ContentInputState.Url(url = url)
        is QRContent.WiFi -> ContentInputState.WiFi(
            ssid = ssid,
            password = password,
            encryption = encryptionType,
        )
        is QRContent.Email -> ContentInputState.Email(
            address = address,
            subject = subject ?: "",
            body = body ?: "",
        )
        is QRContent.Phone -> ContentInputState.Phone(number = number)
        is QRContent.Sms -> ContentInputState.Sms(
            number = number,
            message = message ?: "",
        )
        is QRContent.Contact -> ContentInputState.Text(value = encode())
        is QRContent.Location -> ContentInputState.Text(value = encode())
    }
}
