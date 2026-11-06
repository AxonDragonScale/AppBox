package com.axondragonscale.topty.ui.backuprestore

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.axondragonscale.commons.onFailure
import com.axondragonscale.commons.onSuccess
import com.axondragonscale.topty.backup.BackupFormat
import com.axondragonscale.topty.backup.BackupImportEvent
import com.axondragonscale.topty.backup.BackupImporter
import com.axondragonscale.topty.backup.BackupExporter
import com.axondragonscale.topty.backup.ToptyEncryptionOption
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
            // Export
            is BackupRestoreUiEvent.SelectExportFormat -> onSelectExportFormat(event)
            is BackupRestoreUiEvent.SelectExportEncryption -> onSelectExportEncryption(event)
            is BackupRestoreUiEvent.UpdateCustomExportPassword -> onUpdateCustomExportPassword(event)
            is BackupRestoreUiEvent.ExportBackup -> onExportBackup(event)

            // Import
            is BackupRestoreUiEvent.ImportBackup -> onImportBackup(event)
            is BackupRestoreUiEvent.ConfirmImport -> onConfirmImport(event)
            is BackupRestoreUiEvent.UpdateImportPasswordInput -> uiState.update { it.copy(importPasswordInput = event.password) }
            is BackupRestoreUiEvent.SubmitImportPassword -> onSubmitImportPassword()
            is BackupRestoreUiEvent.DismissImportPasswordDialog -> onDismissImportPasswordDialog()

            // Utility
            is BackupRestoreUiEvent.DismissError -> uiState.update { it.copy(errorMessage = null) }
            is BackupRestoreUiEvent.DismissSuccess -> uiState.update { it.copy(exportSuccess = false, importSuccess = false) }

            // Handled in Compose
            is BackupRestoreUiEvent.NavigateBack -> Unit
        }
    }

    // region Export

    private fun onSelectExportFormat(event: BackupRestoreUiEvent.SelectExportFormat) {
        uiState.update { it.copy(selectedExportFormat = event.format) }
    }

    private fun onSelectExportEncryption(event: BackupRestoreUiEvent.SelectExportEncryption) {
        val currentFormat = uiState.value.selectedExportFormat
        if (currentFormat !is BackupFormat.Topty) return
        uiState.update {
            it.copy(
                selectedExportFormat = currentFormat.copy(encryptionOption = event.option),
                customExportPassword = "",
                customExportPasswordConfirm = "",
            )
        }
    }

    private fun onUpdateCustomExportPassword(event: BackupRestoreUiEvent.UpdateCustomExportPassword) {
        uiState.update {
            val currentFormat = it.selectedExportFormat
            val updatedFormat = if (currentFormat is BackupFormat.Topty &&
                currentFormat.encryptionOption is ToptyEncryptionOption.CustomPassword
            ) {
                currentFormat.copy(encryptionOption = ToptyEncryptionOption.CustomPassword(event.password))
            } else {
                currentFormat
            }
            it.copy(
                selectedExportFormat = updatedFormat,
                customExportPassword = event.password,
                customExportPasswordConfirm = event.confirm,
            )
        }
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

    // endregion

    // region Import

    private fun onImportBackup(event: BackupRestoreUiEvent.ImportBackup) =
        viewModelScope.launch(Dispatchers.IO) {
            uiState.update { it.copy(isLoading = true, errorMessage = null) }

            importer.importFromFile(
                uri = event.uri,
                format = uiState.value.selectedImportFormat,
                eventHandler = ::handleImportEvent,
            )

            uiState.update { it.copy(isLoading = false) }
        }

    private suspend fun handleImportEvent(event: BackupImportEvent) {
        when (event) {
            is BackupImportEvent.PasswordRequired -> {
                uiState.update { it.copy(activeImportEvent = event, importPasswordInput = "") }
            }
            is BackupImportEvent.PasswordError -> {
                uiState.update { it.copy(activeImportEvent = event) }
            }
            is BackupImportEvent.Success -> {
                uiState.update { it.copy(activeImportEvent = null, pendingImportTokens = event.tokens) }
            }
            is BackupImportEvent.Failure -> {
                uiState.update { it.copy(activeImportEvent = null, errorMessage = event.message) }
            }
            is BackupImportEvent.Cancelled -> {
                uiState.update { it.copy(activeImportEvent = null) }
            }
        }
    }

    private fun onSubmitImportPassword() {
        val event = uiState.value.activeImportEvent ?: return
        val password = uiState.value.importPasswordInput
        viewModelScope.launch(Dispatchers.IO) {
            when (event) {
                is BackupImportEvent.PasswordRequired -> event.onReceived(password)
                is BackupImportEvent.PasswordError -> event.onReceived(password)
                else -> Unit
            }
        }
    }

    private fun onDismissImportPasswordDialog() = viewModelScope.launch(Dispatchers.IO) {
        val event = uiState.value.activeImportEvent ?: return@launch
        when (event) {
            is BackupImportEvent.PasswordRequired -> event.onCancel()
            is BackupImportEvent.PasswordError -> event.onCancel()
            else -> Unit
        }
    }

    private fun onConfirmImport(event: BackupRestoreUiEvent.ConfirmImport) =
        viewModelScope.launch(Dispatchers.IO) {
            uiState.update { it.copy(isLoading = true, errorMessage = null) }
            repository.saveTokens(event.tokens)
            uiState.update { it.copy(isLoading = false, importSuccess = true, pendingImportTokens = null) }
        }

    // endregion
}
