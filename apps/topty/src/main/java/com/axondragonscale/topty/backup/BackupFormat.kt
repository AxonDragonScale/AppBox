package com.axondragonscale.topty.backup

/**
 * Created by Ronak Harkhani on 27/01/25
 */

/**
 * Supported backup and restore formats for Topty tokens.
 */
sealed interface BackupFormat {

    /**
     * Topty's native backup format.
     * Includes all token data (name, issuer, group, note, secrets, settings, lastUsedAt).
     * Encryption is configurable via [encryptionOption].
     */
    data class Topty(
        val encryptionOption: ToptyEncryptionOption = ToptyEncryptionOption.AppPassword,
    ) : BackupFormat

    /**
     * Aegis Authenticator JSON format.
     * Industry standard format compatible with Aegis, andOTP, 2FAS, etc.
     * Unencrypted. Does not preserve groups, notes, lastUsedAt, or isFavorite.
     */
    data object Aegis : BackupFormat

    /**
     * Standard OTP Auth URI format.
     * Compatible with most authenticator apps (Google, Microsoft, etc.).
     * Plain text file with one otpauth:// URI per line.
     * Does not preserve groups, notes, lastUsedAt, or isFavorite.
     */
    data object OtpAuthUri : BackupFormat
}

/**
 * Encryption option for the [BackupFormat.Topty] format.
 */
sealed interface ToptyEncryptionOption {

    /**
     * No encryption. The backup payload is plaintext JSON.
     * Anyone with the file can read the token secrets.
     */
    data object None : ToptyEncryptionOption

    /**
     * Encrypted with the app's current master password.
     * The already-derived key and salts are read from [PasswordManager] and [ToptyPrefsRepository]
     * at export time — no password confirmation needed from the user.
     * Restoring on a new device requires the user to enter their app password.
     */
    data object AppPassword : ToptyEncryptionOption

    /**
     * Encrypted with a one-off password set specifically for this backup.
     * The app's master password is unaffected.
     * Restoring requires the user to enter this backup-specific password.
     */
    data class CustomPassword(val password: String) : ToptyEncryptionOption
}
