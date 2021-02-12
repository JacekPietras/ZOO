package com.jacekpietras.zoo.domain.interactor

import com.jacekpietras.zoo.domain.model.MapItemEntity
import com.jacekpietras.zoo.domain.model.PointD
import com.jacekpietras.zoo.domain.repository.GpsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GetTakenRouteUseCase(
    private val gpsRepository: GpsRepository,
) {

    operator fun invoke(): Flow<List<MapItemEntity.PathEntity>> =
        gpsRepository.observeAllPositions().map { each ->
            each.map { list ->
                list.map { PointD(it.lon, it.lat) }
                    .run { MapItemEntity.PathEntity(this) }
            }
        }
}