package com.axondragonscale.appdump.jest.database.converter

import androidx.room.TypeConverter
import com.axondragonscale.appdump.jest.model.Language

/**
 * Created by Ronak Harkhani on 03/04/24
 */
object LanguageConverter {

    @TypeConverter
    fun toLanguage(ordinal: Int) = Language.fromOrdinal(ordinal)

    @TypeConverter
    fun fromLanguage(language: Language) = language.ordinal

}
