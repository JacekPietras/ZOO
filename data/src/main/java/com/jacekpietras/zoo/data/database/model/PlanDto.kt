package com.jacekpietras.zoo.data.database.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "plan")
internal data class PlanDto(
    @PrimaryKey val planId: String,
    val stages: List<StageDto>,
)

internal data class StageDto(
    val regionType: StageRegionType,
    val mutable: Boolean,
    val regionId: String,
    val alternatives: List<String>?,
)

internal enum class StageRegionType {
    ANIMAL,
    RESTAURANT,
    WC,
    EXIT,
}
