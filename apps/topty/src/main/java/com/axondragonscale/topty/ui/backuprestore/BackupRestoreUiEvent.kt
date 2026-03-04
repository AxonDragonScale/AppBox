package com.axondragonscale.topty.ui.backuprestore

import android.net.Uri
import com.axondragonscale.topty.backup.BackupFormat
import com.axondragonscale.topty.backup.ToptyEncryptionOption
import com.axondragonscale.topty.database.entity.Token

/**
 * Created by Ronak Harkhani on 27/01/25
 */

sealed interface BackupRestoreUiEvent {
    data object NavigateBack : BackupRestoreUiEvent

    // Export events
    data class SelectExportFormat(val format: BackupFormat) : BackupRestoreUiEvent
    data class SelectExportEncryption(val option: ToptyEncryptionOption) : BackupRestoreUiEvent
    data class UpdateCustomExportPassword(val password: String, val confirm: String) : BackupRestoreUiEvent
    data class ExportBackup(val uri: Uri) : BackupRestoreUiEvent

    // Import events
    data class ImportBackup(val uri: Uri) : BackupRestoreUiEvent
    data class ConfirmImport(val tokens: List<Token>) : BackupRestoreUiEvent
    data class UpdateImportPasswordInput(val password: String) : BackupRestoreUiEvent
    data object SubmitImportPassword : BackupRestoreUiEvent
    data object DismissImportPasswordDialog : BackupRestoreUiEvent

    // Utility events
    data object DismissError : BackupRestoreUiEvent
    data object DismissSuccess : BackupRestoreUiEvent
}
