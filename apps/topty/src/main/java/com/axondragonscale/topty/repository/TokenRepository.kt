package com.axondragonscale.topty.repository

import com.axondragonscale.commons.logD
import com.axondragonscale.topty.database.dao.TokenDao
import com.axondragonscale.topty.database.entity.Token
import com.axondragonscale.topty.security.TokenEncryption

/**
 * Created by Ronak Harkhani on 14/10/24
 */
class TokenRepository(
    private val dao: TokenDao,
) {
    companion object {
        private const val TAG = "TokenRepository"
    }

    fun getTokens() = dao.getTokens()

    suspend fun getToken(id: Long) = dao.getToken(id)

    suspend fun saveToken(token: Token) = dao.upsert(token)

    suspend fun saveTokens(tokens: List<Token>) = dao.upsert(tokens)

    suspend fun deleteToken(tokenId: Long) = dao.delete(tokenId)

    suspend fun deleteAllTokens() = dao.deleteAllTokens()

    /**
     * Re-encrypts all tokens with a new encryption key. Used when changing password.
     */
    suspend fun reEncryptAllTokens(oldKey: ByteArray, newKey: ByteArray) {
        TAG.logD { "reEncryptAllTokens() start" }
        val reEncryptedTokens = dao.getAllTokens().map { token ->
            val decryptedSecret = TokenEncryption.decrypt(token.encryptedSecret, oldKey) // Decrypt with old key
            val reEncryptedSecret = TokenEncryption.encrypt(decryptedSecret, newKey) // Encrypt with new key

            token.copy(encryptedSecret = reEncryptedSecret)
        }

        // The new tokens must be inserted atomically to prevent observers from triggering midway
        // i.e. when some tokens are new and some are old, which leads to decryption failure
        TAG.logD { "reEncryptAllTokens() inserting reEncryptedTokens" }
        dao.upsert(reEncryptedTokens)
    }

}
