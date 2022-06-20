package com.jacekpietras.zoo.domain.interactor

import com.jacekpietras.core.PointD
import com.jacekpietras.zoo.domain.feature.map.model.MapItemEntity
import com.jacekpietras.zoo.domain.feature.map.repository.MapRepository
import com.jacekpietras.zoo.domain.feature.pathfinder.PathListSnapper
import com.jacekpietras.zoo.domain.feature.pathfinder.interactor.InitializeGraphAnalyzerIfNeededUseCase
import com.jacekpietras.zoo.domain.feature.sensors.model.GpsHistoryEntity
import com.jacekpietras.zoo.domain.feature.sensors.repository.GpsRepository

internal class LoadVisitedRouteUseCaseImpl(
    private val mapRepository: MapRepository,
    private val gpsRepository: GpsRepository,
    private val initializeGraphAnalyzerIfNeededUseCase: InitializeGraphAnalyzerIfNeededUseCase,
    private val pathListSnapper: PathListSnapper,
) : LoadVisitedRouteUseCase {

    override suspend fun run() {
        initializeGraphAnalyzerIfNeededUseCase.run()

        if (!mapRepository.areVisitedRoadsCalculated()) {
            gpsRepository.getAllPositionsNormalized()
                .toPathEntity()
                .let { pathListSnapper.snapToEdges(it) }
                .also { mapRepository.updateVisitedRoads(it) }
        }
    }

    private fun List<List<GpsHistoryEntity>>.toPathEntity() =
        map { list ->
            MapItemEntity.PathEntity(
                list.map { PointD(it.lon, it.lat) }
            )
        }
}
