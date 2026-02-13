package com.axondragonscale.qrqanvas.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.axondragonscale.qrqanvas.database.QRQanvasDatabase.Companion.DB_VERSION
import com.axondragonscale.qrqanvas.database.converter.QRConverters
import com.axondragonscale.qrqanvas.database.dao.QRCodeDao
import com.axondragonscale.qrqanvas.database.dao.ScanHistoryDao
import com.axondragonscale.qrqanvas.database.dao.TemplateDao
import com.axondragonscale.qrqanvas.database.entity.QRCodeEntity
import com.axondragonscale.qrqanvas.database.entity.ScanHistoryEntity
import com.axondragonscale.qrqanvas.database.entity.TemplateEntity

/**
 * Created by Ronak Harkhani on 09/07/25
 */
@Database(
    entities = [
        QRCodeEntity::class,
        ScanHistoryEntity::class,
        TemplateEntity::class,
    ],
    views = [],
    version = DB_VERSION,
    exportSchema = false,
)
@TypeConverters(
    QRConverters::class,
)
internal abstract class QRQanvasDatabase : RoomDatabase() {

    companion object {
        const val DB_VERSION = 3
        const val DB_NAME = "QRQanvasDatabase"

        fun create(appContext: Context): QRQanvasDatabase =
            Room.databaseBuilder(
                context = appContext,
                klass = QRQanvasDatabase::class.java,
                name = DB_NAME,
            )
                .fallbackToDestructiveMigration(dropAllTables = true)
                .build()
    }

    abstract fun qrCodeDao(): QRCodeDao

    abstract fun scanHistoryDao(): ScanHistoryDao

    abstract fun templateDao(): TemplateDao
}
