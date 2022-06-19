package com.jacekpietras.zoo.data.database.converters

import androidx.room.TypeConverter
import com.jacekpietras.zoo.data.database.model.StageDto
import com.jacekpietras.zoo.data.database.utils.fromJson
import com.jacekpietras.zoo.data.database.utils.toJson

internal class ListOfStageConverter {

    @TypeConverter
    fun toValue(value: List<StageDto>): String = value.toJson()

    @TypeConverter
    fun fromString(value: String): List<StageDto> = value.fromJson()
}
