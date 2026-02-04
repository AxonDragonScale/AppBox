package com.axondragonscale.topty.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.axondragonscale.topty.database.ToptyDatabase.Companion.DB_VERSION
import com.axondragonscale.topty.database.dao.TokenDao
import com.axondragonscale.topty.database.entity.Token

/**
 * Created by Ronak Harkhani on 14/10/24
 */
@Database(
    entities = [
        Token::class,
    ],
    version = DB_VERSION,
    exportSchema = true,
)
abstract class ToptyDatabase: RoomDatabase() {

    companion object {
        const val DB_NAME = "topty"
        const val DB_VERSION = 1

        /**
         * Create a new instance of [ToptyDatabase]
         * Must be called only once. Should be called from DI.
         */
        fun create(context: Context): ToptyDatabase =
            Room.databaseBuilder(
                context = context,
                klass = ToptyDatabase::class.java,
                name = DB_NAME
            ).build()
    }

    abstract fun codeDao(): TokenDao

}
