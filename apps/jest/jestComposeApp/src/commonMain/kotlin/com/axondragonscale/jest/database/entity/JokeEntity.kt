package com.axondragonscale.jest.database.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Index
import androidx.room.Index.Order
import androidx.room.PrimaryKey
import com.axondragonscale.jest.model.Category
import com.axondragonscale.jest.model.Flags
import com.axondragonscale.jest.model.JokeType
import com.axondragonscale.jest.model.Language

/**
 * Created by Ronak Harkhani on 03/04/24
 */
@Entity(
    tableName = "joke",
    indices = [
        Index("timestamp", unique = true, orders = [Order.DESC]),
        Index("favorite", "timestamp", unique = true, orders = [Order.DESC, Order.DESC])
    ],
)
data class JokeEntity(
    @PrimaryKey val id: Int,
    val timestamp: Long,
    val lang: Language,
    val category: Category,
    val type: JokeType,
    @Embedded val flags: Flags,
    val safe: Boolean,
    val favorite: Boolean,
    val joke: String?,
    val setup: String?,
    val delivery: String?,
)
