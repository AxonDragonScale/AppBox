package com.axondragonscale.topty.backup.topty

import com.axondragonscale.topty.backup.ToptyEncryptionOption
import com.axondragonscale.topty.database.entity.Token
import com.axondragonscale.topty.repository.ToptyPrefsRepository
import com.axondragonscale.topty.security.PasswordManager
import com.axondragonscale.topty.security.TokenEncryption
import kotlinx.serialization.json.Json

/**
 * Created by Ronak Harkhani on 27/01/25
 */

/**
 * Handles exporting tokens to the Topty native backup format.
 * Encryption behaviour is determined by [BackupFormat.Topty.encryptionOption].
 */
class ToptyBackupExporter(
    private val json: Json,
    private val passwordManager: PasswordManager,
    private val prefsRepository: ToptyPrefsRepository,
) {

    /**
     * Serializes [tokens] into a Topty backup file string, applying the encryption
     * strategy specified by [encryptionOption].
     */
    suspend fun export(tokens: List<Token>, encryptionOption: ToptyEncryptionOption): String {
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
}
