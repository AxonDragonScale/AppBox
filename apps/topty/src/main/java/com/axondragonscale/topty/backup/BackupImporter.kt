package com.axondragonscale.topty.backup

import android.content.Context
import android.net.Uri
import com.axondragonscale.topty.core.TokenUriHandler
import com.axondragonscale.topty.database.entity.Token
import com.axondragonscale.topty.model.HashAlgo
import com.axondragonscale.topty.model.Protocol
import com.axondragonscale.topty.security.PasswordManager
import com.axondragonscale.topty.security.TokenEncryption
import kotlinx.serialization.json.Json

/**
 * Created by Ronak Harkhani on 27/01/25
 */

/**
 * Handles importing tokens from various backup formats.
 */
class BackupImporter(
    private val context: Context,
    private val json: Json,
    private val passwordManager: PasswordManager,
) {

    /**
     * Import tokens from the backup file at [uri].
     *
     * All outcomes — including success, failure, cancellation, and mid-flight password prompts —
     * are communicated exclusively via [eventHandler]. The method returns [Unit].
     *
     * For TOPTY format:
     * - [BackupEncryptionType.NONE]: decodes payload directly, emits [BackupImportEvent.Success].
     * - [BackupEncryptionType.ENCRYPTED]: tries the current app key first (same-device fast path).
     *   If that fails, emits [BackupImportEvent.PasswordRequired] and suspends until the user
     *   provides a password via the event's callbacks.
     *   Subsequent wrong passwords emit [BackupImportEvent.PasswordError].
     *
     * For AEGIS and OTP_AUTH_URI formats, only terminal events are emitted.
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
                is BackupFormat.Topty -> importToptyFormat(content, eventHandler)
                is BackupFormat.Aegis -> eventHandler(BackupImportEvent.Success(importAegisFormat(content)))
                is BackupFormat.OtpAuthUri -> eventHandler(BackupImportEvent.Success(importOtpAuthUriFormat(content)))
            }
        } catch (e: Exception) {
            eventHandler(BackupImportEvent.Failure("Failed to import backup: ${e.message}"))
        }
    }

    // region Topty Format

    private suspend fun importToptyFormat(
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
            eventHandler(BackupImportEvent.Failure(
                "Backup version ${backupFile.version} is not supported. Please update the app."
            ))
            return
        }

        when (backupFile.encryptionType) {
            BackupEncryptionType.NONE -> decodeAndEmit(backupFile.payload, eventHandler)
            BackupEncryptionType.ENCRYPTED -> importEncryptedToptyFormat(backupFile, eventHandler)
        }
    }

    private suspend fun importEncryptedToptyFormat(
        backupFile: ToptyBackupFile,
        eventHandler: suspend (BackupImportEvent) -> Unit,
    ) {
        // Same-device Same-install fast path: try the current app key first
        val decryptedPayload = try {
            TokenEncryption.decrypt(backupFile.payload, passwordManager.getEncryptionKey())
        } catch (e: Exception) {
            null // App key didn't work, fall through to password prompt
        }

        if (decryptedPayload != null) {
            decodeAndEmit(decryptedPayload, eventHandler)
            return
        } else {
            // App key failed — need the user to provide a password
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

    // endregion

    // region Aegis Format

    private fun importAegisFormat(content: String): List<Token> {
        val vault = json.decodeFromString<AegisVault>(content)

        if (vault.version > 1) {
            throw IllegalArgumentException("Aegis vault version ${vault.version} is not supported")
        }

        if (!vault.header.slots.isNullOrEmpty()) {
            throw IllegalArgumentException(
                "Encrypted Aegis vaults are not supported. Please decrypt in Aegis first."
            )
        }

        return vault.db.entries.map { entry ->
            val protocol = when (entry.type.lowercase()) {
                "totp" -> Protocol.TOTP
                "hotp" -> Protocol.HOTP
                else -> throw IllegalArgumentException("Unsupported protocol: ${entry.type}")
            }

            val hashAlgo = when (entry.info.algo.uppercase()) {
                "SHA1" -> HashAlgo.SHA1
                "SHA256" -> HashAlgo.SHA256
                "SHA512" -> HashAlgo.SHA512
                else -> HashAlgo.SHA1
            }

            Token.fromPlaintext(
                id = 0,
                name = entry.name,
                issuer = entry.issuer,
                group = entry.group ?: "",
                note = "",
                secret = entry.info.secret,
                protocol = protocol,
                hashAlgo = hashAlgo,
                period = entry.info.period ?: 30,
                counter = entry.info.counter ?: 0L,
                digits = entry.info.digits,
                lastUsedAt = 0L,
            )
        }
    }

    // endregion

    // region OTP Auth URI Format

    private fun importOtpAuthUriFormat(content: String): List<Token> {
        val tokens = content.lines()
            .map { it.trim() }
            .filter { it.isNotEmpty() && it.startsWith("otpauth://") }
            .mapNotNull { TokenUriHandler.parseTokenUri(it) }

        if (tokens.isEmpty()) throw IllegalArgumentException("No valid OTP Auth URIs found in the backup file")
        return tokens
    }

    // endregion

    private fun readFile(uri: Uri): String? =
        context.contentResolver.openInputStream(uri)?.use { it.bufferedReader().readText() }
}
