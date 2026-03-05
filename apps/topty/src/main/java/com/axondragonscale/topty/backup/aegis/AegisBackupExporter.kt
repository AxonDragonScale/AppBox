package com.axondragonscale.topty.backup.aegis

import com.axondragonscale.topty.database.entity.Token
import com.axondragonscale.topty.model.Protocol
import kotlinx.serialization.json.Json
import java.util.UUID

/**
 * Created by Ronak Harkhani on 27/01/25
 */

/**
 * Handles exporting tokens to the Aegis JSON vault format.
 * Produces an unencrypted Aegis vault compatible with Aegis, andOTP, 2FAS, and similar apps.
 */
class AegisBackupExporter(
    private val json: Json,
) {

    /**
     * Serializes [tokens] into an Aegis vault JSON string.
     */
    fun export(tokens: List<Token>): String {
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
}
