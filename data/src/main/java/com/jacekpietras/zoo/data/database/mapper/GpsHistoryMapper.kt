package com.jacekpietras.zoo.data.database.mapper

import com.jacekpietras.zoo.data.database.model.GpsHistoryDto
import com.jacekpietras.zoo.domain.model.GpsHistoryEntity

internal class GpsHistoryMapper {

    fun from(entity: GpsHistoryEntity): GpsHistoryDto =
        GpsHistoryDto(
            timestamp = entity.timestamp,
            lat = entity.lat,
            lon = entity.lon
        )

    fun from(dto: GpsHistoryDto): GpsHistoryEntity =
        GpsHistoryEntity(
            timestamp = dto.timestamp,
            lat = dto.lat,
            lon = dto.lon
        )
}