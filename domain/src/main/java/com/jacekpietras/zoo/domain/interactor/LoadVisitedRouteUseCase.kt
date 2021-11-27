package com.jacekpietras.zoo.domain.interactor

import com.jacekpietras.core.PointD
import com.jacekpietras.zoo.domain.business.PathListSnapper
import com.jacekpietras.zoo.domain.model.GpsHistoryEntity
import com.jacekpietras.zoo.domain.model.MapItemEntity.PathEntity
import com.jacekpietras.zoo.domain.repository.GpsRepository
import com.jacekpietras.zoo.domain.repository.MapRepository

class LoadVisitedRouteUseCase(
    private val mapRepository: MapRepository,
    private val gpsRepository: GpsRepository,
    private val initializeGraphAnalyzerIfNeededUseCase: InitializeGraphAnalyzerIfNeededUseCase,
) {

    suspend fun run() {
        initializeGraphAnalyzerIfNeededUseCase.run()

        if (!mapRepository.areVisitedRoadsCalculated()) {
            gpsRepository.getAllPositionsNormalized()
                .toPathEntity()
                .let { PathListSnapper().snapToEdges(it) }
                .also { mapRepository.updateVisitedRoads(it) }
        }
    }

    private fun List<List<GpsHistoryEntity>>.toPathEntity() =
        map { list ->
            PathEntity(
                list.map { PointD(it.lon, it.lat) }
            )
        }
}
