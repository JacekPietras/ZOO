package com.jacekpietras.zoo.domain.interactor

import com.jacekpietras.core.PointD
import com.jacekpietras.zoo.domain.business.PathListSnapper
import com.jacekpietras.zoo.domain.business.PathSnapper
import com.jacekpietras.zoo.domain.model.GpsHistoryEntity
import com.jacekpietras.zoo.domain.model.MapItemEntity
import com.jacekpietras.zoo.domain.repository.GpsRepository
import com.jacekpietras.zoo.domain.repository.MapRepository

class InsertUserPositionUseCase(
    private val gpsRepository: GpsRepository,
    private val worldBoundsUseCase: GetWorldBoundsUseCase,
    private val mapRepository: MapRepository,
) {

    private var lastPosition: GpsHistoryEntity? = null
    private val pathListSnapper = PathListSnapper()
    private val pathSnapper = PathSnapper()

    suspend fun run(position: GpsHistoryEntity) {
        val bounds = worldBoundsUseCase.run()
        if (bounds.contains(position.lon, position.lat)) {
            if (lastPosition == null) {
                lastPosition = gpsRepository.getAllPositions().lastOrNull()
            }

            gpsRepository.insertPosition(position)

            addVisitedPart(lastPosition, position)

            lastPosition = position
        }
    }

    private suspend fun addVisitedPart(prev: GpsHistoryEntity?, next: GpsHistoryEntity) {
        prev ?: return
        if (!mapRepository.areVisitedRoadsCalculated()) return

        val path = MapItemEntity.PathEntity(
            listOf(
                PointD(prev.lon, prev.lat),
                PointD(next.lon, next.lat)
            )
        )
        val snappedEdge = pathSnapper.snapToEdges(path)

        val alreadyVisited = checkNotNull(mapRepository.getVisitedRoads())
        val updated = pathListSnapper.merge(alreadyVisited, snappedEdge)
        mapRepository.updateVisitedRoads(updated)
    }
}
