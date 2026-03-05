package com.axondragonscale.topty.backup.aegis

import com.axondragonscale.topty.backup.BackupImportEvent
import com.axondragonscale.topty.database.entity.Token
import com.axondragonscale.topty.model.HashAlgo
import com.axondragonscale.topty.model.Protocol
import kotlinx.serialization.json.Json

/**
 * Created by Ronak Harkhani on 27/01/25
 */

/**
 * Handles importing tokens from the Aegis JSON vault format.
 * Only unencrypted vaults are supported — encrypted vaults must be decrypted in Aegis first.
 */
class AegisBackupImporter(
    private val json: Json,
) {

    /**
     * Parses an Aegis vault JSON string and emits [BackupImportEvent.Success] or
     * [BackupImportEvent.Failure] via [eventHandler].
     */
    suspend fun import(
        content: String,
        eventHandler: suspend (BackupImportEvent) -> Unit,
    ) {
        try {
            val vault = json.decodeFromString<AegisVault>(content)

            if (vault.version > 1) {
                eventHandler(BackupImportEvent.Failure("Aegis vault version ${vault.version} is not supported"))
                return
            }

            if (!vault.header.slots.isNullOrEmpty()) {
                eventHandler(BackupImportEvent.Failure("Encrypted Aegis vaults are not supported. Please decrypt in Aegis first."))
                return
            }

            val tokens = vault.db.entries.map { entry ->
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

            eventHandler(BackupImportEvent.Success(tokens))
        } catch (e: Exception) {
            eventHandler(BackupImportEvent.Failure("Failed to import Aegis backup: ${e.message}"))
        }
    }
}
