package com.axondragonscale.topty.backup

import android.content.Context
import android.net.Uri
import com.axondragonscale.commons.Result
import com.axondragonscale.topty.core.TokenUriHandler
import com.axondragonscale.topty.database.entity.Token
import com.axondragonscale.topty.model.Protocol
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
) {

    /**
     * Export tokens to the specified format.
     *
     * @param tokens List of tokens to export
     * @param format Backup format to use
     * @return Backup content as a string
     */
    suspend fun export(tokens: List<Token>, format: BackupFormat): Result<String, String> {
        return try {
            val content = when (format) {
                BackupFormat.TOPTY -> exportToptyFormat(tokens)
                BackupFormat.AEGIS -> exportAegisFormat(tokens)
                BackupFormat.OTP_AUTH_URI -> exportOtpAuthUriFormat(tokens)
            }
            Result.Success(content)
        } catch (e: Exception) {
            Result.Failure("Failed to export backup: ${e.message}")
        }
    }

    /**
     * Export tokens to file at the specified URI.
     *
     * @param tokens List of tokens to export
     * @param format Backup format to use
     * @param uri URI where the backup should be written
     * @return Result indicating success or failure
     */
    suspend fun exportToFile(tokens: List<Token>, format: BackupFormat, uri: Uri): Result<Unit, String> {
        return try {
            val content = when (format) {
                BackupFormat.TOPTY -> exportToptyFormat(tokens)
                BackupFormat.AEGIS -> exportAegisFormat(tokens)
                BackupFormat.OTP_AUTH_URI -> exportOtpAuthUriFormat(tokens)
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

    /**
     * Export to Topty's native encrypted JSON format.
     * Includes all token data.
     */
    private fun exportToptyFormat(tokens: List<Token>): String {
        val backupTokens = tokens.map { token ->
            BackupToken(
                name = token.name,
                issuer = token.issuer,
                group = token.group,
                note = token.note,
                encryptedSecret = token.encryptedSecret,
                protocol = token.protocol,
                hashAlgo = token.hashAlgo,
                period = token.period,
                counter = token.counter,
                digits = token.digits,
                lastUsedAt = token.lastUsedAt
            )
        }

        val backupData = ToptyBackupData(tokenCount = tokens.size, tokens = backupTokens)
        return TokenEncryption.encrypt(json.encodeToString(backupData))
    }

    /**
     * Export to Aegis Authenticator encrypted JSON format.
     * Compatible with Aegis, andOTP, 2FAS, and other apps.
     * Note: This format does not preserve notes or lastUsedAt.
     * For simplicity, we're creating an unencrypted Aegis vault.
     * Users can encrypt it in Aegis if needed.
     */
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

        val database = AegisDatabase(
            version = 2,
            entries = aegisEntries
        )

        val vault = AegisVault(
            version = 1,
            header = AegisHeader(
                slots = null,
                params = null
            ),
            db = database
        )

        return json.encodeToString(vault)
    }

    /**
     * Export to OTP Auth URI format.
     * Plain text file with one otpauth:// URI per line.
     * Compatible with most authenticator apps.
     *
     * Note: This format does not preserve groups, notes, or lastUsedAt.
     */
    private fun exportOtpAuthUriFormat(tokens: List<Token>): String {
        return tokens.joinToString("\n") { token ->
            TokenUriHandler.generateTokenUri(token)
        }
    }
}
