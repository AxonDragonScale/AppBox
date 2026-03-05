package com.axondragonscale.topty.backup.topty

import com.axondragonscale.topty.backup.BackupImportEvent
import com.axondragonscale.topty.database.entity.Token
import com.axondragonscale.topty.security.PasswordManager
import com.axondragonscale.topty.security.TokenEncryption
import kotlinx.serialization.json.Json

/**
 * Created by Ronak Harkhani on 27/01/25
 */

/**
 * Handles importing tokens from the Topty native backup format.
 *
 * - [BackupEncryptionType.NONE]: decodes payload directly, emits [BackupImportEvent.Success].
 * - [BackupEncryptionType.ENCRYPTED]: tries the current app key first (same-device fast path).
 *   If that fails, emits [BackupImportEvent.PasswordRequired] and suspends until the user
 *   provides a password via the event's callbacks.
 *   Subsequent wrong passwords emit [BackupImportEvent.PasswordError].
 */
class ToptyBackupImporter(
    private val json: Json,
    private val passwordManager: PasswordManager,
) {

    /**
     * Parses and imports a Topty backup from [content].
     * All outcomes are communicated via [eventHandler].
     */
    suspend fun import(
        content: String,
        eventHandler: suspend (BackupImportEvent) -> Unit,
    ) {
        val backupFile = try {
            json.decodeFromString<ToptyBackupFile>(content)
        } catch (e: Exception) {
            eventHandler(BackupImportEvent.Failure("Invalid backup file format"))
            return
        }

        if (backupFile.version > ToptyBackupFile.CURRENT_VERSION) {
            eventHandler(
                BackupImportEvent.Failure(
                    "Backup version ${backupFile.version} is not supported. Please update the app."
                )
            )
            return
        }

        when (backupFile.encryptionType) {
            BackupEncryptionType.NONE -> decodeAndEmit(backupFile.payload, eventHandler)
            BackupEncryptionType.ENCRYPTED -> importEncrypted(backupFile, eventHandler)
        }
    }

    private suspend fun importEncrypted(
        backupFile: ToptyBackupFile,
        eventHandler: suspend (BackupImportEvent) -> Unit,
    ) {
        // Same-device same-install fast path: try the current app key first
        val decryptedPayload = try {
            TokenEncryption.decrypt(backupFile.payload, passwordManager.getEncryptionKey())
        } catch (e: Exception) {
            null // App key didn't work, fall through to password prompt
        }

        if (decryptedPayload != null) {
            decodeAndEmit(decryptedPayload, eventHandler)
        } else {
            requestPassword(backupFile, eventHandler)
        }
    }

    private suspend fun requestPassword(
        backupFile: ToptyBackupFile,
        eventHandler: suspend (BackupImportEvent) -> Unit,
    ) {
        eventHandler(BackupImportEvent.PasswordRequired(
            onReceived = { password -> decryptWithPassword(password, backupFile, eventHandler) },
            onCancel = { eventHandler(BackupImportEvent.Cancelled) },
        ))
    }

    private suspend fun decryptWithPassword(
        password: String,
        backupFile: ToptyBackupFile,
        eventHandler: suspend (BackupImportEvent) -> Unit,
    ) {
        try {
            if (!verifyPassword(password, backupFile)) {
                eventHandler(BackupImportEvent.PasswordError(
                    message = "Incorrect password. Please try again.",
                    onReceived = { retryPassword -> decryptWithPassword(retryPassword, backupFile, eventHandler) },
                    onCancel = { eventHandler(BackupImportEvent.Cancelled) },
                ))
                return
            }

            // Hash matched — derive key and decrypt
            val key = passwordManager.deriveKey(password, backupFile.keyDerivationSalt!!)
            val plaintext = TokenEncryption.decrypt(backupFile.payload, key)
            decodeAndEmit(plaintext, eventHandler)
        } catch (e: Exception) {
            eventHandler(BackupImportEvent.Failure("Failed to decrypt backup: ${e.message}"))
        }
    }

    private fun verifyPassword(password: String, backupFile: ToptyBackupFile): Boolean {
        val saltBase64 = backupFile.passwordVerificationSalt ?: return false
        val expectedHash = backupFile.passwordVerificationHash ?: return false
        return passwordManager.verifyPasswordAgainstHash(password, expectedHash, saltBase64)
    }

    private suspend fun decodeAndEmit(
        payloadJson: String,
        eventHandler: suspend (BackupImportEvent) -> Unit,
    ) {
        try {
            val payload = json.decodeFromString<ToptyBackupPayload>(payloadJson)
            val tokens = payload.tokens.map { backupToken ->
                Token.fromPlaintext(
                    id = 0,
                    name = backupToken.name,
                    issuer = backupToken.issuer,
                    group = backupToken.group,
                    note = backupToken.note,
                    secret = backupToken.secret,
                    protocol = backupToken.protocol,
                    hashAlgo = backupToken.hashAlgo,
                    period = backupToken.period,
                    counter = backupToken.counter,
                    digits = backupToken.digits,
                    lastUsedAt = backupToken.lastUsedAt,
                )
            }
            eventHandler(BackupImportEvent.Success(tokens))
        } catch (e: Exception) {
            eventHandler(BackupImportEvent.Failure("Failed to parse backup payload: ${e.message}"))
        }
    }
}
