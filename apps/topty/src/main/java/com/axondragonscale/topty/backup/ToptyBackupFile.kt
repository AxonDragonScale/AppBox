package com.axondragonscale.topty.backup

import kotlinx.serialization.Serializable

/**
 * Created by Ronak Harkhani on 06 February 2026.
 */

/**
 * Outer structure of a Topty (.topty) backup file written to and read from disk.
 * Always serialized as plaintext JSON regardless of encryption mode, so the importer
 * can read the metadata without needing to decrypt anything first.
 *
 * When [encryptionType] is [BackupEncryptionType.NONE], [payload] is a plaintext JSON
 * string of [ToptyBackupPayload].
 *
 * When [encryptionType] is [BackupEncryptionType.ENCRYPTED], [payload] is an AES-256-GCM
 * encrypted + Base64-encoded JSON string of [ToptyBackupPayload]. The key is derived via
 * PBKDF2 from a password (either the app password or a custom backup password) using
 * [keyDerivationSalt]. All data needed to re-derive the key and verify the password on
 * any device is embedded here.
 */
@Serializable
data class ToptyBackupFile(
    val version: Int = CURRENT_VERSION,
    val timestamp: Long = System.currentTimeMillis(),
    val encryptionType: BackupEncryptionType,

    // Present when encryptionType == ENCRYPTED:

    /** Base64-encoded PBKDF2 salt used to derive the encryption key from the password. */
    val keyDerivationSalt: String? = null,

    /**
     * Base64-encoded hash used for fast password verification before attempting decryption.
     * Computed as PBKDF2(password, [passwordVerificationSalt]).
     * For APP_PASSWORD exports this is the existing passwordHash from DataStore.
     * For CUSTOM_PASSWORD exports this is freshly computed at export time.
     */
    val passwordVerificationHash: String? = null,

    /** Base64-encoded salt used to compute [passwordVerificationHash]. */
    val passwordVerificationSalt: String? = null,

    /**
     * The backup payload.
     * Plaintext JSON of [ToptyBackupPayload] when [encryptionType] is [BackupEncryptionType.NONE].
     * AES-256-GCM encrypted + Base64-encoded JSON of [ToptyBackupPayload] when
     * [encryptionType] is [BackupEncryptionType.ENCRYPTED].
     */
    val payload: String,
) {
    companion object {
        const val CURRENT_VERSION = 2
    }
}
