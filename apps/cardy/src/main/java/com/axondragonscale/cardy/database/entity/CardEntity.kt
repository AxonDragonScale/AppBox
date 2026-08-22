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
)
