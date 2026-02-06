package com.axondragonscale.topty.ui.backuprestore

import android.net.Uri
import com.axondragonscale.topty.backup.BackupFormat
import com.axondragonscale.topty.database.entity.Token

/**
 * Created by Ronak Harkhani on 27/01/25
 */

sealed interface BackupRestoreUiEvent {
    data object NavigateBack : BackupRestoreUiEvent

    // Export events
    data class SelectExportFormat(val format: BackupFormat) : BackupRestoreUiEvent
    data class ExportBackup(val uri: Uri) : BackupRestoreUiEvent

    // Import events
    data class SelectImportFormat(val format: BackupFormat) : BackupRestoreUiEvent
    data class ImportBackup(val uri: Uri) : BackupRestoreUiEvent
    data class ConfirmImport(val tokens: List<Token>) : BackupRestoreUiEvent

    // Utility events
    data object DismissError : BackupRestoreUiEvent
    data object DismissSuccess : BackupRestoreUiEvent
}
