package com.axondragonscale.qrqanvas.ui.templates

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.axondragonscale.qrqanvas.core.ThumbnailGenerator
import com.axondragonscale.qrqanvas.model.QRContent
import com.axondragonscale.qrqanvas.model.Template
import com.axondragonscale.qrqanvas.repository.TemplateRepository
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
class TemplatesVM(
    private val templateRepository: TemplateRepository,
) : ViewModel() {

    val uiState = MutableStateFlow(TemplatesUiState())

    private val uiEffectChannel = Channel<TemplatesUiEffect>(Channel.BUFFERED)
    val uiEffect = uiEffectChannel.receiveAsFlow().flowOn(Dispatchers.Main.immediate)

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

    private fun observeTemplates() = viewModelScope.launch {
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
                val dummyContent = QRContent.Text("QRQanvas")
                val bitmap = ThumbnailGenerator.generate(dummyContent, template.style)
                if (bitmap != null) {
                    uiState.update { state ->
                        state.copy(templateThumbnails = state.templateThumbnails + (template.id to bitmap))
                    }
                }
                generatingIds.remove(template.id)
            }
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
            uiEffectChannel.send(TemplatesUiEffect.ShowSnackbar("Template renamed to \"$newName\""))
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
            uiEffectChannel.send(TemplatesUiEffect.ShowSnackbar("Template \"$name\" deleted"))
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
}
