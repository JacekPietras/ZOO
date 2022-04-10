package com.jacekpietras.zoo.data.database.converters

import androidx.room.TypeConverter
import com.jacekpietras.zoo.data.database.utils.fromJson
import com.jacekpietras.zoo.data.database.utils.toJson

internal class ListOfStringsConverter {

    @TypeConverter
    fun toValue(value: List<String>): String = value.toJson()

    @TypeConverter
    fun fromString(value: String): List<String> = value.fromJson()
}
