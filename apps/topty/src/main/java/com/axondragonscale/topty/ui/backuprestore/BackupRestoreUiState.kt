package com.axondragonscale.topty.ui.backuprestore

import com.axondragonscale.topty.backup.BackupFormat
import com.axondragonscale.topty.backup.BackupImportEvent
import com.axondragonscale.topty.backup.ToptyEncryptionOption
import com.axondragonscale.topty.database.entity.Token

/**
 * Created by Ronak Harkhani on 27/01/25
 */
data class BackupRestoreUiState(
    val isLoading: Boolean = false,
    val tokenCount: Int = 0,

    // Export
    val selectedExportFormat: BackupFormat = BackupFormat.Topty(),
    val customExportPassword: String = "",
    val customExportPasswordConfirm: String = "",
    val exportSuccess: Boolean = false,

    // Import
    val selectedImportFormat: BackupFormat = BackupFormat.Topty(),
    val importSuccess: Boolean = false,
    val pendingImportTokens: List<Token>? = null,
    val activeImportEvent: BackupImportEvent? = null,  // non-null = show password dialog
    val importPasswordInput: String = "",

    val errorMessage: String? = null,
) {
    val exportUiState get() = ExportUiState(
        selectedFormat = selectedExportFormat,
        customPassword = customExportPassword,
        customPasswordConfirm = customExportPasswordConfirm,
        isLoading = isLoading,
        exportSuccess = exportSuccess,
    )

    val importUiState get() = ImportUiState(
        isLoading = isLoading,
        importSuccess = importSuccess,
        pendingImportTokens = pendingImportTokens,
        activeImportEvent = activeImportEvent,
        importPasswordInput = importPasswordInput,
    )
}

/**
 * Sub-state for the export section of the Backup & Restore screen.
 */
data class ExportUiState(
    val selectedFormat: BackupFormat = BackupFormat.Topty(),
    val customPassword: String = "",
    val customPasswordConfirm: String = "",
    val isLoading: Boolean = false,
    val exportSuccess: Boolean = false,
)

/**
 * Sub-state for the import section of the Backup & Restore screen.
 */
data class ImportUiState(
    val isLoading: Boolean = false,
    val importSuccess: Boolean = false,
    val pendingImportTokens: List<Token>? = null,
    val activeImportEvent: BackupImportEvent? = null,
    val importPasswordInput: String = "",
)
