package com.jacekpietras.zoo.domain.feature.sensors.model

data class GpsHistoryEntity(
    val timestamp: Long,
    val lat: Double,
    val lon: Double,
)
