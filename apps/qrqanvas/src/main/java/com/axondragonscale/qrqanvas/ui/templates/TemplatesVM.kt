package com.axondragonscale.qrqanvas.ui.templates

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.axondragonscale.qrqanvas.core.QRGenerator
import com.axondragonscale.qrqanvas.core.QRRenderer
import com.axondragonscale.qrqanvas.model.QRContent
import com.axondragonscale.qrqanvas.model.Template
import com.axondragonscale.qrqanvas.repository.TemplateRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Created by Ronak Harkhani on 09/07/25
 */
class TemplatesVM(
    private val templateRepository: TemplateRepository,
    private val qrGenerator: QRGenerator,
    private val qrRenderer: QRRenderer,
) : ViewModel() {

    val uiState = MutableStateFlow(TemplatesUiState())

    private val _effects = Channel<TemplatesUiEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    /** Tracks which template IDs already have a thumbnail being generated. */
    private val generatingIds = mutableSetOf<String>()

    init {
        observeTemplates()
    }

    fun onEvent(event: TemplatesUiEvent): Unit = when (event) {
        is TemplatesUiEvent.RenameClicked -> onRenameClicked(event.templateId, event.currentName)
        is TemplatesUiEvent.RenameNameChanged -> onRenameNameChanged(event.name)
        is TemplatesUiEvent.ConfirmRename -> onConfirmRename()
        is TemplatesUiEvent.DismissRename -> onDismissRename()
        is TemplatesUiEvent.DeleteClicked -> onDeleteClicked(event.templateId, event.name)
        is TemplatesUiEvent.ConfirmDelete -> onConfirmDelete()
        is TemplatesUiEvent.DismissDelete -> onDismissDelete()
    }

    private fun observeTemplates() {
        viewModelScope.launch {
            templateRepository.getAll().collect { allTemplates ->
                val predefined = allTemplates.filter { it.isPredefined }
                val user = allTemplates.filter { !it.isPredefined }
                uiState.update {
                    it.copy(
                        predefinedTemplates = predefined,
                        userTemplates = user,
                    )
                }
                generateMissingThumbnails(allTemplates)
            }
        }
    }

    /**
     * Generates QR code thumbnails for templates that don't already have one cached.
     * Uses a dummy content string and a small output size for fast rendering.
     */
    private fun generateMissingThumbnails(templates: List<Template>) {
        val currentThumbnails = uiState.value.templateThumbnails
        val missing = templates.filter { it.id !in currentThumbnails && it.id !in generatingIds }
        if (missing.isEmpty()) return

        // Remove thumbnails for deleted templates
        val activeIds = templates.map { it.id }.toSet()
        if (currentThumbnails.keys.any { it !in activeIds }) {
            uiState.update { state ->
                state.copy(templateThumbnails = state.templateThumbnails.filterKeys { it in activeIds })
            }
        }

        for (template in missing) {
            generatingIds.add(template.id)
            viewModelScope.launch(Dispatchers.Default) {
                val bitmap = renderThumbnail(template)
                if (bitmap != null) {
                    uiState.update { state ->
                        state.copy(templateThumbnails = state.templateThumbnails + (template.id to bitmap))
                    }
                }
                generatingIds.remove(template.id)
            }
        }
    }

    /**
     * Renders a small QR code thumbnail from the template's style.
     */
    private fun renderThumbnail(template: Template): Bitmap? {
        return try {
            val dummyContent = QRContent.Text("QRQanvas")
            val style = template.style.copy(size = THUMBNAIL_SIZE, quietZone = 1, logoConfig = null)
            val bitMatrix = qrGenerator.generate(dummyContent, style.errorCorrection)
            qrRenderer.render(bitMatrix, style)
        } catch (e: Exception) {
            null
        }
    }

    private fun onRenameClicked(templateId: String, currentName: String) {
        uiState.update {
            it.copy(
                showRenameDialog = true,
                renameTemplateId = templateId,
                renameTemplateName = currentName,
            )
        }
    }

    private fun onRenameNameChanged(name: String) {
        uiState.update { it.copy(renameTemplateName = name) }
    }

    private fun onConfirmRename() {
        val templateId = uiState.value.renameTemplateId ?: return
        val newName = uiState.value.renameTemplateName.trim()
        if (newName.isEmpty()) return

        uiState.update {
            it.copy(
                showRenameDialog = false,
                renameTemplateId = null,
                renameTemplateName = "",
            )
        }

        viewModelScope.launch(Dispatchers.IO) {
            templateRepository.rename(templateId, newName)
            _effects.send(TemplatesUiEffect.ShowSnackbar("Template renamed to \"$newName\""))
        }
    }

    private fun onDismissRename() {
        uiState.update {
            it.copy(
                showRenameDialog = false,
                renameTemplateId = null,
                renameTemplateName = "",
            )
        }
    }

    private fun onDeleteClicked(templateId: String, name: String) {
        uiState.update {
            it.copy(
                showDeleteDialog = true,
                deleteTemplateId = templateId,
                deleteTemplateName = name,
            )
        }
    }

    private fun onConfirmDelete() {
        val templateId = uiState.value.deleteTemplateId ?: return
        val name = uiState.value.deleteTemplateName

        uiState.update {
            it.copy(
                showDeleteDialog = false,
                deleteTemplateId = null,
                deleteTemplateName = "",
            )
        }

        viewModelScope.launch(Dispatchers.IO) {
            templateRepository.delete(templateId)
            _effects.send(TemplatesUiEffect.ShowSnackbar("Template \"$name\" deleted"))
        }
    }

    private fun onDismissDelete() {
        uiState.update {
            it.copy(
                showDeleteDialog = false,
                deleteTemplateId = null,
                deleteTemplateName = "",
            )
        }
    }

    companion object {
        private const val THUMBNAIL_SIZE = 200
    }
}
