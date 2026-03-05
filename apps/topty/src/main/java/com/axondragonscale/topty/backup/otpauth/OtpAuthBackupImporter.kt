package com.axondragonscale.topty.backup.otpauth

import com.axondragonscale.topty.backup.BackupImportEvent
import com.axondragonscale.topty.core.TokenUriHandler
import com.axondragonscale.topty.database.entity.Token

/**
 * Created by Ronak Harkhani on 27/01/25
 */

/**
 * Handles importing tokens from the OTP Auth URI format.
 * Parses a plain text file containing one otpauth:// URI per line.
 */
class OtpAuthBackupImporter {

    /**
     * Parses a newline-delimited string of otpauth:// URIs and emits [BackupImportEvent.Success]
     * or [BackupImportEvent.Failure] via [eventHandler].
     */
    suspend fun import(
        content: String,
        eventHandler: suspend (BackupImportEvent) -> Unit,
    ) {
        try {
            val tokens = content.lines()
                .map { it.trim() }
                .filter { it.isNotEmpty() && it.startsWith("otpauth://") }
                .mapNotNull { TokenUriHandler.parseTokenUri(it) }

            if (tokens.isEmpty()) {
                eventHandler(BackupImportEvent.Failure("No valid OTP Auth URIs found in the backup file"))
                return
            }

            eventHandler(BackupImportEvent.Success(tokens))
        } catch (e: Exception) {
            eventHandler(BackupImportEvent.Failure("Failed to import OTP Auth backup: ${e.message}"))
        }
    }
}
