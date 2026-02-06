package com.axondragonscale.topty.backup

import com.axondragonscale.topty.model.HashAlgo
import com.axondragonscale.topty.model.Protocol
import kotlinx.serialization.Serializable

/**
 * Created by Ronak Harkhani on 27/01/25
 */

/**
 * Root backup data structure for Topty native format.
 * Contains metadata and all tokens.
 */
@Serializable
data class ToptyBackupData(
    val version: Int = CURRENT_VERSION,
    val timestamp: Long = System.currentTimeMillis(),
    val tokenCount: Int,
    val tokens: List<BackupToken>,
) {
    companion object {
        const val CURRENT_VERSION = 1
    }
}

/**
 * Token data for backup.
 * Contains all token information including encrypted secret.
 */
@Serializable
data class BackupToken(
    val name: String,
    val issuer: String,
    val group: String,
    val note: String,
    val encryptedSecret: String,
    val protocol: Protocol,
    val hashAlgo: HashAlgo,
    val period: Int,
    val counter: Long,
    val digits: Int,
    val lastUsedAt: Long,
)
