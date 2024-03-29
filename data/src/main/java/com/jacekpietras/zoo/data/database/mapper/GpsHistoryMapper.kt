package com.jacekpietras.zoo.data.database.mapper

import com.jacekpietras.zoo.data.database.model.GpsHistoryDto
import com.jacekpietras.zoo.domain.feature.sensors.model.GpsHistoryEntity

internal class GpsHistoryMapper {

    fun from(entity: GpsHistoryEntity): GpsHistoryDto =
        GpsHistoryDto(
            timestamp = entity.timestamp,
            lat = entity.lat,
            lon = entity.lon,
            accuracy = entity.accuracy,
        )

    fun from(dto: GpsHistoryDto): GpsHistoryEntity =
        GpsHistoryEntity(
            timestamp = dto.timestamp,
            lat = dto.lat,
            lon = dto.lon,
            accuracy = dto.accuracy,
        )
}