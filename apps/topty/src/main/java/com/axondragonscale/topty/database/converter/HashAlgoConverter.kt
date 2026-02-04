package com.axondragonscale.topty.database.converter

import androidx.room.TypeConverter
import com.axondragonscale.topty.model.HashAlgo

/**
 * Created by Ronak Harkhani on 27/10/24
 */
object HashAlgoConverter {

    @TypeConverter
    fun fromHashAlgo(hashAlgo: HashAlgo): Int = hashAlgo.ordinal

    @TypeConverter
    fun toHashAlgo(ordinal: Int): HashAlgo = HashAlgo.entries[ordinal]

}
