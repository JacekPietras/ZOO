package com.jacekpietras.zoo.domain.model

data class GpsHistoryEntity(
    val timestamp: Long,
    val lat: Double,
    val lon: Double,
)
