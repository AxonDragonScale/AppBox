package com.axondragonscale.topty.backup

import android.content.Context
import android.net.Uri
import com.axondragonscale.commons.Result
import com.axondragonscale.topty.backup.aegis.AegisBackupExporter
import com.axondragonscale.topty.backup.otpauth.OtpAuthBackupExporter
import com.axondragonscale.topty.backup.topty.ToptyBackupExporter
import com.axondragonscale.topty.database.entity.Token
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

/**
 * Created by Ronak Harkhani on 27/01/25
 */

/**
 * Facade that dispatches token export to the appropriate format-specific handler
 * and writes the result to the file at the given URI.
 */
class BackupExporter(
    private val context: Context,
): KoinComponent {

    /**
     * Export [tokens] to the specified [format] and write to the file at [uri].
     */
    suspend fun exportToFile(tokens: List<Token>, format: BackupFormat, uri: Uri): Result<Unit, String> {
        return try {
            val content = when (format) {
                is BackupFormat.Topty -> get<ToptyBackupExporter>().export(tokens, format.encryptionOption)
                is BackupFormat.Aegis -> get<AegisBackupExporter>().export(tokens)
                is BackupFormat.OtpAuthUri -> get<OtpAuthBackupExporter>().export(tokens)
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
}
