package com.axondragonscale.jest.database.converter

import androidx.room.TypeConverter
import com.axondragonscale.jest.model.Category

/**
 * Created by Ronak Harkhani on 03/04/24
 */
object CategoryConverter {

    @TypeConverter
    fun toCategory(ordinal: Int) = Category.fromOrdinal(ordinal)

    @TypeConverter
    fun fromCategory(category: Category) = category.ordinal

}
