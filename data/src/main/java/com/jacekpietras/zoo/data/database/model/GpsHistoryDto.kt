package com.jacekpietras.zoo.data.database.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "gps_history")
internal data class GpsHistoryDto(
    @PrimaryKey val timestamp: Long,
    val lat: Double,
    val lon: Double,
    val accuracy: Float,
)
