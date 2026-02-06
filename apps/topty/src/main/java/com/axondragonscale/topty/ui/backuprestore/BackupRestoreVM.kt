package com.axondragonscale.topty.ui.backuprestore

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.axondragonscale.topty.backup.BackupExporter
import com.axondragonscale.topty.backup.BackupImporter
import com.axondragonscale.topty.repository.TokenRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Created by Ronak Harkhani on 27/01/25
 */
class BackupRestoreVM(
    private val repository: TokenRepository,
    private val exporter: BackupExporter,
    private val importer: BackupImporter,
) : ViewModel() {

    val uiState = MutableStateFlow(BackupRestoreUiState())

    init {
        viewModelScope.launch(Dispatchers.IO) {
            repository.getTokens().collect { tokens ->
                uiState.update { it.copy(tokenCount = tokens.size) }
            }
        }
    }

    fun onEvent(event: BackupRestoreUiEvent) {
        when (event) {
            is BackupRestoreUiEvent.SelectExportFormat -> onSelectExportFormat(event)
            is BackupRestoreUiEvent.ExportBackup -> onExportBackup(event)

            is BackupRestoreUiEvent.SelectImportFormat -> onSelectImportFormat(event)
            is BackupRestoreUiEvent.ImportBackup -> onImportBackup(event)
            is BackupRestoreUiEvent.ConfirmImport -> onConfirmImport(event)

            is BackupRestoreUiEvent.DismissError -> uiState.update { it.copy(errorMessage = null, pendingImportTokens = null) }
            is BackupRestoreUiEvent.DismissSuccess -> uiState.update { it.copy(exportSuccess = false, importSuccess = false) }

            // Handled in Compose
            is BackupRestoreUiEvent.NavigateBack -> Unit
        }
    }

    private fun onSelectExportFormat(event: BackupRestoreUiEvent.SelectExportFormat) {
        uiState.update { it.copy(selectedExportFormat = event.format) }
    }

    private fun onExportBackup(event: BackupRestoreUiEvent.ExportBackup) =
        viewModelScope.launch(Dispatchers.IO) {
            uiState.update { it.copy(isLoading = true, errorMessage = null) }

            val tokens = repository.getTokens().first()
            if (tokens.isEmpty()) {
                uiState.update { it.copy(isLoading = false, errorMessage = "No tokens to export") }
                return@launch
            }

            exporter.exportToFile(tokens, uiState.value.selectedExportFormat, event.uri)
                .onSuccess {
                    uiState.update { it.copy(isLoading = false, exportSuccess = true) }
                }
                .onFailure { message ->
                    uiState.update { it.copy(isLoading = false, errorMessage = message) }
                }
        }

    private fun onSelectImportFormat(event: BackupRestoreUiEvent.SelectImportFormat) {
        uiState.update { it.copy(selectedImportFormat = event.format) }
    }

    private fun onImportBackup(event: BackupRestoreUiEvent.ImportBackup) =
        viewModelScope.launch(Dispatchers.IO) {
            uiState.update { it.copy(isLoading = true, errorMessage = null) }

            importer.importFromFile(event.uri, uiState.value.selectedImportFormat)
                .onSuccess { tokens ->
                    uiState.update {
                        it.copy(isLoading = false, pendingImportTokens = tokens)
                    }
                }
                .onFailure { message ->
                    uiState.update { it.copy(isLoading = false, errorMessage = message) }
                }
        }

    private fun onConfirmImport(event: BackupRestoreUiEvent.ConfirmImport) =
        viewModelScope.launch(Dispatchers.IO) {
            uiState.update { it.copy(isLoading = true, errorMessage = null) }

            repository.saveTokens(event.tokens)

            uiState.update {
                it.copy(isLoading = false, importSuccess = true, pendingImportTokens = null)
            }
        }

}
