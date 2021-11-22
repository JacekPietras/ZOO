package com.jacekpietras.zoo.domain.interactor

import com.jacekpietras.core.PointD
import com.jacekpietras.zoo.domain.model.GpsHistoryEntity
import com.jacekpietras.zoo.domain.model.MapItemEntity
import com.jacekpietras.zoo.domain.repository.GpsRepository
import com.jacekpietras.zoo.domain.repository.MapRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class ObserveVisitedRoadsUseCase(
    private val mapRepository: MapRepository,
    private val gpsRepository: GpsRepository,
    private val getSnapPathToRoadUseCase: GetSnapPathToRoadUseCase,
) {

    suspend fun run(): Flow<List<MapItemEntity.PathEntity>> {
//        if (!mapRepository.areVisitedRoadsCalculated()) {
//            coroutineScope {
//                launch {
//                    val historicalGpsData = gpsRepository.getAllPositionsNormalized().toPathEntity()
//                    val snapped = getSnapPathToRoadUseCase.run(historicalGpsData)
////                    mapRepository.updateVisitedRoads(snapped)
//                    mapRepository.updateVisitedRoads(emptyList())
//                }
//            }
//        }
        return flowOf(emptyList())
//        return mapRepository.getVisitedRoads()
//            .map { MapItemEntity.PathEntity(it) }
    }

    private fun List<List<GpsHistoryEntity>>.toPathEntity() =
        map { list ->
            MapItemEntity.PathEntity(
                list.map { PointD(it.lon, it.lat) }
            )
        }
}