/** Created by Ronak on 05/30/26 */
package com.axondragonscale.cardy.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.axondragonscale.cardy.database.dao.CardDao
import com.axondragonscale.cardy.database.entity.CardEntity

@Database(entities = [CardEntity::class], version = 1)
abstract class CardyDatabase : RoomDatabase() {

    companion object {
        const val DB_NAME = "CardyDatabase"

        fun create(context: Context): CardyDatabase =
            Room.databaseBuilder(context, CardyDatabase::class.java, DB_NAME).build()
    }

    abstract fun cardDao(): CardDao
}
