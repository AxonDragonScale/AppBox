/** Created by Ronak on 05/30/26 */
package com.axondragonscale.cardy.repository

import com.axondragonscale.cardy.database.dao.CardDao
import com.axondragonscale.cardy.database.entity.CardEntity
import com.axondragonscale.cardy.model.CardReadResult
import com.axondragonscale.cardy.security.CardEncryption
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class CardRepository(private val cardDao: CardDao) {

    fun getAllCards(): Flow<List<CardEntity>> = cardDao.getAllCards()

    suspend fun getCardById(id: Long): CardEntity? = cardDao.getCardById(id)

    suspend fun saveCard(result: CardReadResult): Long {
        val serialized = Json.encodeToString(result)
        val encrypted = CardEncryption.encryptString(serialized)
        val entity = CardEntity(
            label = result.displayLabel,
            network = result.primaryNetwork.name,
            lastFourDigits = result.primaryPan?.takeLast(4) ?: "????",
            expiry = result.primaryExpiry,
            timestamp = result.timestamp,
            encryptedData = encrypted,
        )
        return cardDao.insertCard(entity)
    }

    suspend fun decryptCardData(entity: CardEntity): CardReadResult? = try {
        val json = CardEncryption.decryptString(entity.encryptedData)
        Json.decodeFromString<CardReadResult>(json)
    } catch (e: Exception) {
        null
    }

    suspend fun updateLabel(id: Long, newLabel: String) {
        val card = cardDao.getCardById(id) ?: return
        cardDao.updateCard(card.copy(label = newLabel))
    }

    suspend fun deleteCard(id: Long) = cardDao.deleteCard(id)

    suspend fun deleteAllCards() = cardDao.deleteAllCards()
}
