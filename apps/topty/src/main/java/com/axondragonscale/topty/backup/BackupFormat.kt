package com.axondragonscale.topty.backup

/**
 * Created by Ronak Harkhani on 27/01/25
 */

/**
 * Supported backup and restore formats for Topty tokens.
 */
enum class BackupFormat(
    val displayName: String,
    val fileExtension: String,
    val description: String,
) {
    /**
     * Topty's native encrypted backup format.
     * Includes all token data (name, issuer, group, note, secrets, settings, lastUsedAt).
     * Encrypted for security.
     */
    TOPTY(
        displayName = "Topty (Encrypted)",
        fileExtension = "topty",
        description = "Topty's encrypted backup with all token data"
    ),

    /**
     * Aegis Authenticator encrypted JSON format.
     * Industry standard format compatible with Aegis, andOTP, 2FAS, etc.
     * Encrypted with password.
     * Includes groups but not notes or lastUsedAt.
     */
    AEGIS(
        displayName = "Aegis (Encrypted)",
        fileExtension = "json",
        description = "Aegis encrypted format compatible with many apps"
    ),

    /**
     * Standard OTP Auth URI format.
     * Compatible with most authenticator apps (Google, Microsoft, etc.).
     * Plain text file with one otpauth:// URI per line.
     * Note: Does not include groups, notes, or lastUsedAt.
     */
    OTP_AUTH_URI(
        displayName = "OTP Auth URI (Plain Text)",
        fileExtension = "txt",
        description = "Standard otpauth:// URIs compatible with most apps"
    );

}
