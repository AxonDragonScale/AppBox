package com.axondragonscale.topty.backup

import android.content.Context
import android.net.Uri
import com.axondragonscale.commons.Result
import com.axondragonscale.topty.core.TokenUriHandler
import com.axondragonscale.topty.database.entity.Token
import com.axondragonscale.topty.model.Protocol
import com.axondragonscale.topty.repository.ToptyPrefsRepository
import com.axondragonscale.topty.security.PasswordManager
import com.axondragonscale.topty.security.TokenEncryption
import kotlinx.serialization.json.Json
import java.util.UUID

/**
 * Created by Ronak Harkhani on 27/01/25
 */

/**
 * Handles exporting tokens to various backup formats.
 */
class BackupExporter(
    private val context: Context,
    private val json: Json,
    private val passwordManager: PasswordManager,
    private val prefsRepository: ToptyPrefsRepository,
) {

    /**
     * Export tokens to the specified format and write to the file at [uri].
     */
    suspend fun exportToFile(tokens: List<Token>, format: BackupFormat, uri: Uri): Result<Unit, String> {
        return try {
            val content = when (format) {
                is BackupFormat.Topty -> exportToptyFormat(tokens, format.encryptionOption)
                is BackupFormat.Aegis -> exportAegisFormat(tokens)
                is BackupFormat.OtpAuthUri -> exportOtpAuthUriFormat(tokens)
            }

            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                outputStream.write(content.toByteArray())
                outputStream.flush()
            } ?: return Result.Failure("Failed to open output stream")

            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Failure("Failed to export backup: ${e.message}")
        }
    }

    // region Topty Format

    private suspend fun exportToptyFormat(tokens: List<Token>, encryptionOption: ToptyEncryptionOption): String {
        val payload = buildPayload(tokens)
        val payloadJson = json.encodeToString(payload)

        val backupFile = when (encryptionOption) {
            is ToptyEncryptionOption.None -> buildUnencryptedBackupFile(payloadJson)
            is ToptyEncryptionOption.AppPassword -> buildAppPasswordBackupFile(payloadJson)
            is ToptyEncryptionOption.CustomPassword -> buildCustomPasswordBackupFile(payloadJson, encryptionOption.password)
        }

        return json.encodeToString(backupFile)
    }

    private fun buildPayload(tokens: List<Token>) = ToptyBackupPayload(
        tokens = tokens.map { token ->
            BackupToken(
                name = token.name,
                issuer = token.issuer,
                group = token.group,
                note = token.note,
                secret = token.secret,
                protocol = token.protocol,
                hashAlgo = token.hashAlgo,
                period = token.period,
                counter = token.counter,
                digits = token.digits,
                lastUsedAt = token.lastUsedAt,
            )
        }
    )

    private fun buildUnencryptedBackupFile(payloadJson: String) = ToptyBackupFile(
        encryptionType = BackupEncryptionType.NONE,
        payload = payloadJson,
    )

    private suspend fun buildAppPasswordBackupFile(payloadJson: String): ToptyBackupFile {
        val key = passwordManager.getEncryptionKey()
        val encryptedPayload = TokenEncryption.encrypt(payloadJson, key)

        // Reuse the existing password hash and salts already stored in DataStore —
        // no need to re-derive anything, and the user doesn't need to re-enter their password.
        val keyDerivationSalt = prefsRepository.keyDerivationSalt.get()
        val passwordVerificationHash = prefsRepository.passwordHash.get()
        val passwordVerificationSalt = prefsRepository.passwordHashSalt.get()

        return ToptyBackupFile(
            encryptionType = BackupEncryptionType.ENCRYPTED,
            keyDerivationSalt = keyDerivationSalt,
            passwordVerificationHash = passwordVerificationHash,
            passwordVerificationSalt = passwordVerificationSalt,
            payload = encryptedPayload,
        )
    }

    private fun buildCustomPasswordBackupFile(payloadJson: String, password: String): ToptyBackupFile {
        val derived = passwordManager.derivePasswordData(password)
        val encryptedPayload = TokenEncryption.encrypt(payloadJson, derived.key)

        return ToptyBackupFile(
            encryptionType = BackupEncryptionType.ENCRYPTED,
            keyDerivationSalt = derived.keyDerivationSalt,
            passwordVerificationHash = derived.passwordVerificationHash,
            passwordVerificationSalt = derived.passwordVerificationSalt,
            payload = encryptedPayload,
        )
    }

    // endregion

    // region Aegis Format

    private fun exportAegisFormat(tokens: List<Token>): String {
        val aegisEntries = tokens.map { token ->
            AegisEntry(
                type = token.protocol.code.lowercase(),
                uuid = UUID.randomUUID().toString(),
                name = token.name,
                issuer = token.issuer,
                group = token.group.takeIf { it.isNotBlank() },
                icon = null,
                info = AegisInfo(
                    secret = token.secret,
                    algo = token.hashAlgo.name,
                    digits = token.digits,
                    period = if (token.protocol == Protocol.TOTP) token.period else null,
                    counter = if (token.protocol == Protocol.HOTP) token.counter else null,
                )
            )
        }

        val database = AegisDatabase(version = 2, entries = aegisEntries)
        val vault = AegisVault(
            version = 1,
            header = AegisHeader(slots = null, params = null),
            db = database,
        )

        return json.encodeToString(vault)
    }

    // endregion

    // region OTP Auth URI Format

    private fun exportOtpAuthUriFormat(tokens: List<Token>): String =
        tokens.joinToString("\n") { token -> TokenUriHandler.generateTokenUri(token) }

    // endregion
}
