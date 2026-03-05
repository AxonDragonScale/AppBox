package com.axondragonscale.topty.backup.topty

import com.axondragonscale.topty.model.HashAlgo
import com.axondragonscale.topty.model.Protocol
import kotlinx.serialization.Serializable

/**
 * Created by Ronak Harkhani on 27/01/25
 */

/**
 * Inner payload of a Topty backup file.
 * Contains all token data. Serialized to JSON and either stored as plaintext or
 * encrypted inside [ToptyBackupFile]. Version and timestamp live on the envelope.
 */
@Serializable
data class ToptyBackupPayload(
    val tokens: List<BackupToken>,
)

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
    val secret: String,
    val protocol: Protocol,
    val hashAlgo: HashAlgo,
    val period: Int,
    val counter: Long,
    val digits: Int,
    val lastUsedAt: Long,
)
