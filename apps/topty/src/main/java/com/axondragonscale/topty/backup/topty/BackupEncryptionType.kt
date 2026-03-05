package com.axondragonscale.topty.backup.topty

/**
 * Created by Ronak Harkhani on 06 February 2026.
 */

/**
 * Encryption type for a Topty backup file.
 * Stored in the plaintext envelope so the importer can detect it without decrypting.
 */
enum class BackupEncryptionType {
    /** Payload is plaintext JSON. No password needed to restore. */
    NONE,

    /**
     * Payload is AES-256-GCM encrypted.
     * Key is derived via PBKDF2 from a password (either the app password or a custom backup password).
     * All salts and verification data needed to restore are embedded in the envelope.
     */
    ENCRYPTED,
}
