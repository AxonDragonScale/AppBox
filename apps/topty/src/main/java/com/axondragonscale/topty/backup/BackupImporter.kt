package com.axondragonscale.topty.backup

import android.content.Context
import android.net.Uri
import com.axondragonscale.topty.backup.aegis.AegisBackupImporter
import com.axondragonscale.topty.backup.otpauth.OtpAuthBackupImporter
import com.axondragonscale.topty.backup.topty.ToptyBackupImporter
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

/**
 * Created by Ronak Harkhani on 27/01/25
 */

/**
 * Facade that dispatches token import to the appropriate format-specific handler.
 *
 * All outcomes — including success, failure, cancellation, and mid-flight password prompts —
 * are communicated exclusively via [eventHandler]. The method returns [Unit].
 */
class BackupImporter(
    private val context: Context,
) : KoinComponent {

    /**
     * Import tokens from the backup file at [uri].
     * The [format] determines which handler is used. All outcomes are communicated via [eventHandler].
     */
    suspend fun importFromFile(
        uri: Uri,
        format: BackupFormat,
        eventHandler: suspend (BackupImportEvent) -> Unit,
    ) {
        try {
            val content = readFile(uri)
            if (content == null) {
                eventHandler(BackupImportEvent.Failure("Failed to open file"))
                return
            }
            if (content.isEmpty()) {
                eventHandler(BackupImportEvent.Failure("Backup file is empty"))
                return
            }

            when (format) {
                is BackupFormat.Topty -> get<ToptyBackupImporter>().import(content, eventHandler)
                is BackupFormat.Aegis -> get<AegisBackupImporter>().import(content, eventHandler)
                is BackupFormat.OtpAuthUri -> get<OtpAuthBackupImporter>().import(content, eventHandler)
            }
        } catch (e: Exception) {
            eventHandler(BackupImportEvent.Failure("Failed to import backup: ${e.message}"))
        }
    }

    private fun readFile(uri: Uri): String? =
        context.contentResolver.openInputStream(uri)?.use { it.bufferedReader().readText() }
}
