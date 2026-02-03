package com.axondragonscale.topty.database.converter

import androidx.room.TypeConverter
import com.axondragonscale.topty.model.Protocol

/**
 * Created by Ronak Harkhani on 27/10/24
 */
object ProtocolConverter {

    @TypeConverter
    fun fromProtocol(protocol: Protocol): Int = protocol.ordinal

    @TypeConverter
    fun toProtocol(ordinal: Int): Protocol = Protocol.entries[ordinal]

}
