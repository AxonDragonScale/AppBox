package com.axondragonscale.topty.backup.otpauth

import com.axondragonscale.topty.core.TokenUriHandler
import com.axondragonscale.topty.database.entity.Token

/**
 * Created by Ronak Harkhani on 27/01/25
 */

/**
 * Handles exporting tokens to the OTP Auth URI format.
 * Produces a plain text file with one otpauth:// URI per line,
 * compatible with most authenticator apps.
 */
class OtpAuthBackupExporter {

    /**
     * Serializes [tokens] into a newline-delimited string of otpauth:// URIs.
     */
    fun export(tokens: List<Token>): String =
        tokens.joinToString("\n") { token -> TokenUriHandler.generateTokenUri(token) }
}
