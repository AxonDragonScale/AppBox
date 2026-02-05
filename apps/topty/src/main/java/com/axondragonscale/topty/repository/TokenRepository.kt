package com.axondragonscale.topty.repository

import com.axondragonscale.topty.database.dao.TokenDao
import com.axondragonscale.topty.database.entity.Token

/**
 * Created by Ronak Harkhani on 14/10/24
 */
class TokenRepository(
    private val dao: TokenDao,
) {

    fun getTokens() = dao.getTokens()

    suspend fun getToken(id: Long) = dao.getToken(id)

    suspend fun saveToken(token: Token) = dao.upsert(token)

    suspend fun deleteToken(tokenId: Long) = dao.delete(tokenId)

}
