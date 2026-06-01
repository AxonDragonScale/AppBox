/** Created by Ronak on 05/30/26 */
package com.axondragonscale.cardy.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cards")
data class CardEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val label: String,
    val network: String,
    val lastFourDigits: String,
    val expiry: String?,
    val timestamp: Long,
    val encryptedData: ByteArray,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is CardEntity) return false
        return id == other.id &&
            label == other.label &&
            network == other.network &&
            lastFourDigits == other.lastFourDigits &&
            expiry == other.expiry &&
            timestamp == other.timestamp &&
            encryptedData.contentEquals(other.encryptedData)
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + label.hashCode()
        result = 31 * result + network.hashCode()
        result = 31 * result + lastFourDigits.hashCode()
        result = 31 * result + (expiry?.hashCode() ?: 0)
        result = 31 * result + timestamp.hashCode()
        result = 31 * result + encryptedData.contentHashCode()
        return result
    }
}
