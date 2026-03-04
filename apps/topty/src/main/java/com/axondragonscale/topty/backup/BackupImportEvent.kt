package com.axondragonscale.topty.backup

import com.axondragonscale.topty.database.entity.Token

/**
 * Created by Ronak Harkhani on 06 February 2026.
 */

/**
 * Events emitted by [BackupImporter] during import via the eventHandler lambda.
 * Sealed for exhaustive handling and future extensibility.
 *
 * Both mid-flight interactions (password prompts) and terminal outcomes (success, failure,
 * cancellation) are communicated through this type, making the eventHandler the single
 * channel for all import state.
 */
sealed interface BackupImportEvent {

    // region Mid-flight interactions

    /**
     * The backup is encrypted and the current app key failed to decrypt it.
     * Show a password input dialog. Call [onReceived] with the entered password to continue,
     * or [onCancel] to abort the import.
     */
    data class PasswordRequired(
        val onReceived: suspend (String) -> Unit,
        val onCancel: suspend () -> Unit,
    ) : BackupImportEvent

    /**
     * The password entered in response to [PasswordRequired] or a previous [PasswordError]
     * was incorrect. Show an inline error and keep the dialog open.
     * Call [onReceived] with a new password to retry, or [onCancel] to abort.
     */
    data class PasswordError(
        val message: String,
        val onReceived: suspend (String) -> Unit,
        val onCancel: suspend () -> Unit,
    ) : BackupImportEvent

    // endregion

    // region Terminal states

    /**
     * Import completed successfully.
     * [tokens] are ready to be confirmed and saved by the caller.
     */
    data class Success(val tokens: List<Token>) : BackupImportEvent

    /**
     * Import failed due to an unrecoverable error (bad format, unsupported version,
     * decryption failure, etc.).
     */
    data class Failure(val message: String) : BackupImportEvent

    /**
     * User cancelled the import by dismissing the password dialog.
     */
    data object Cancelled : BackupImportEvent

    // endregion
}
