package com.jacekpietras.zoo.domain.interactor

import com.jacekpietras.core.PointD
import com.jacekpietras.zoo.domain.feature.map.model.MapItemEntity
import com.jacekpietras.zoo.domain.feature.sensors.model.GpsHistoryEntity
import com.jacekpietras.zoo.domain.feature.sensors.repository.GpsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ObserveOldTakenRouteUseCase(
    private val gpsRepository: GpsRepository,
) {

    fun run(): Flow<List<MapItemEntity.PathEntity>> =
        gpsRepository.observeOldPositions()
            .map { each ->
                each
                    .map(::toPoints)
                    .map(MapItemEntity::PathEntity)
            }

    private fun toPointD(point: GpsHistoryEntity): PointD = PointD(point.lon, point.lat)

    private fun toPoints(list: List<GpsHistoryEntity>) = list.map(::toPointD)

}
