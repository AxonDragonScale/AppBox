package com.axondragonscale.topty.backup

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Created by Ronak Harkhani on 27/01/25
 *
 * Aegis Authenticator vault format.
 * Spec: https://github.com/beemdevelopment/Aegis/blob/master/docs/vault.md
 */

/**
 * Root Aegis vault structure.
 */
@Serializable
data class AegisVault(
    val version: Int = 1,
    val header: AegisHeader,
    val db: AegisDatabase,
)

/**
 * Aegis vault header containing encryption parameters.
 * For plain/unencrypted vaults, slots are empty.
 */
@Serializable
data class AegisHeader(
    val slots: List<AegisSlot>? = null,
    val params: AegisParams? = null,
)

/**
 * Encryption slot for Aegis vault.
 * Each slot represents a way to decrypt the vault (password, biometric, etc.)
 */
@Serializable
data class AegisSlot(
    val type: Int = 1, // 1 = password, 2 = biometric
    val uuid: String,
    val key: String,
    @SerialName("key_params")
    val keyParams: AegisParams,
)

/**
 * Encryption/key derivation parameters.
 */
@Serializable
data class AegisParams(
    val nonce: String,
    val tag: String,
)

/**
 * Aegis database containing entries.
 * This is the encrypted payload in encrypted vaults.
 */
@Serializable
data class AegisDatabase(
    val version: Int = 2,
    val entries: List<AegisEntry>,
)

/**
 * Single Aegis entry (token).
 */
@Serializable
data class AegisEntry(
    val type: String, // "totp" or "hotp"
    val uuid: String,
    val name: String,
    val issuer: String,
    val group: String? = null,
    val icon: String? = null,
    val info: AegisInfo,
)

/**
 * Token information for Aegis entry.
 */
@Serializable
data class AegisInfo(
    val secret: String,
    val algo: String, // "SHA1", "SHA256", "SHA512"
    val digits: Int,
    val period: Int? = null, // For TOTP
    val counter: Long? = null, // For HOTP
)
