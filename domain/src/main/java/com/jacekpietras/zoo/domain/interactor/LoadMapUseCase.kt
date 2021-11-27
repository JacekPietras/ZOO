package com.jacekpietras.zoo.domain.interactor

import com.jacekpietras.core.PointD
import com.jacekpietras.zoo.domain.model.GpsHistoryEntity
import com.jacekpietras.zoo.domain.model.MapItemEntity.PathEntity
import com.jacekpietras.zoo.domain.repository.GpsRepository
import com.jacekpietras.zoo.domain.repository.MapRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

class LoadMapUseCase(
    private val mapRepository: MapRepository,
    private val gpsRepository: GpsRepository,
    private val getSnapPathToRoadUseCase: GetSnapPathToRoadUseCase,
) {

    suspend fun run() = coroutineScope {
        listOf(
            async(Dispatchers.Default) {
                if (!mapRepository.isMapLoaded()) {
                    mapRepository.loadMap()
                }
            },
            async(Dispatchers.Default) {
                if (!mapRepository.areVisitedRoadsCalculated()) {
                    gpsRepository.getAllPositionsNormalized()
                        .toPathEntity()
                        .let { getSnapPathToRoadUseCase.run(it) }
                        .also { mapRepository.updateVisitedRoads(it) }
                }
            },
        ).awaitAll()
    }

    private fun List<List<GpsHistoryEntity>>.toPathEntity() =
        map { list ->
            PathEntity(
                list.map { PointD(it.lon, it.lat) }
            )
        }
}