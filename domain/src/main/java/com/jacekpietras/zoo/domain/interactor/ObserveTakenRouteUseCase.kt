package com.jacekpietras.zoo.domain.interactor

import com.jacekpietras.core.PointD
import com.jacekpietras.zoo.domain.model.MapItemEntity
import com.jacekpietras.zoo.domain.repository.GpsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ObserveTakenRouteUseCase(
    private val gpsRepository: GpsRepository,
) {

    fun run(): Flow<List<MapItemEntity.PathEntity>> =
        gpsRepository.observeAllPositions().map { each ->
            each.map { list ->
                MapItemEntity.PathEntity(
                    list.map { PointD(it.lon, it.lat) }
                )
            }
        }
}