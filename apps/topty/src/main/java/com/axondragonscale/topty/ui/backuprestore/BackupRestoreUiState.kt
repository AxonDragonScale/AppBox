package com.axondragonscale.topty.ui.backuprestore

import com.axondragonscale.topty.backup.BackupFormat
import com.axondragonscale.topty.database.entity.Token

/**
 * Created by Ronak Harkhani on 27/01/25
 */
data class BackupRestoreUiState(
    val isLoading: Boolean = false,
    val tokenCount: Int = 0,

    val selectedExportFormat: BackupFormat = BackupFormat.TOPTY,
    val exportSuccess: Boolean = false,

    val selectedImportFormat: BackupFormat = BackupFormat.TOPTY,
    val importSuccess: Boolean = false,
    val pendingImportTokens: List<Token>? = null,

    val errorMessage: String? = null,
)
