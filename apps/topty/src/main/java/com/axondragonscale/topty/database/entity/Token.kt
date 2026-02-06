package com.axondragonscale.topty.database.entity

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.axondragonscale.topty.model.HashAlgo
import com.axondragonscale.topty.model.Protocol
import com.axondragonscale.topty.security.SecretEncryption

/**
 * Created by Ronak Harkhani on 14/10/24
 */
@Entity(tableName = "token")
data class Token(
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0,
    val name: String,
    val issuer: String,
    val group: String,
    val note: String,
    val encryptedSecret: String,
    val protocol: Protocol,
    val hashAlgo: HashAlgo,
    val period: Int, // TODO: Convert to duration class ?
    val counter: Long,
    val digits: Int,
) {

    /**
     * Helper property to get/set the plaintext secret.
     * Automatically decrypts on get.
     */
    @get:Ignore
    val secret: String
        get() = if (encryptedSecret.isEmpty()) "" else SecretEncryption.decrypt(encryptedSecret)

    /**
     * Creates a copy of this token with a new secret (automatically encrypted).
     * Use this instead of directly setting the secret property.
     */
    fun withSecret(plainSecret: String): Token {
        return copy(encryptedSecret = SecretEncryption.encrypt(plainSecret))
    }

    companion object {
        /**
         * Creates an empty token with no encrypted secret.
         */
        fun emptyToken() = Token(
            name = "",
            issuer = "",
            group = "",
            note = "",
            encryptedSecret = "",
            protocol = Protocol.TOTP,
            hashAlgo = HashAlgo.SHA1,
            period = 30,
            counter = 0L,
            digits = 6
        )

        /**
         * Creates a token from plaintext values (encrypts the secret).
         */
        fun fromPlaintext(
            id: Long = 0,
            name: String,
            issuer: String,
            group: String,
            note: String,
            secret: String,
            protocol: Protocol,
            hashAlgo: HashAlgo,
            period: Int,
            counter: Long,
            digits: Int,
        ): Token {
            return Token(
                id = id,
                name = name,
                issuer = issuer,
                group = group,
                note = note,
                encryptedSecret = SecretEncryption.encrypt(secret),
                protocol = protocol,
                hashAlgo = hashAlgo,
                period = period,
                counter = counter,
                digits = digits,
            )
        }
    }

}
