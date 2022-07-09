package com.jacekpietras.zoo.domain.feature.sensors.interactor

import com.jacekpietras.geometry.PointD
import com.jacekpietras.geometry.RectD
import com.jacekpietras.geometry.haversine
import com.jacekpietras.zoo.domain.feature.map.interactor.GetWorldBoundsUseCase
import com.jacekpietras.zoo.domain.feature.map.model.MapItemEntity
import com.jacekpietras.zoo.domain.feature.map.repository.MapRepository
import com.jacekpietras.zoo.domain.feature.pathfinder.PathListSnapper
import com.jacekpietras.zoo.domain.feature.pathfinder.PathSnapper
import com.jacekpietras.zoo.domain.feature.sensors.model.GpsHistoryEntity
import com.jacekpietras.zoo.domain.feature.sensors.repository.GpsEventsRepository
import com.jacekpietras.zoo.domain.feature.sensors.repository.GpsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

internal class InsertUserPositionUseCaseImpl(
    private val gpsRepository: GpsRepository,
    private val worldBoundsUseCase: GetWorldBoundsUseCase,
    private val mapRepository: MapRepository,
    private val stopNavigationUseCase: StopNavigationUseCase,
    private val pathListSnapper: PathListSnapper,
    private val pathSnapper: PathSnapper,
    private val gpsEventsRepository: GpsEventsRepository,
) : InsertUserPositionUseCase {

    override fun run(position: GpsHistoryEntity) {
        CoroutineScope(Dispatchers.IO).launch {
            val bounds = worldBoundsUseCase.run()
            if (bounds.contains(position)) {
                gpsRepository.insertPosition(position)
                addVisitedPart(position)
            } else {
                val distanceToWorld = haversine(
                    bounds.centerX(),
                    bounds.centerY(),
                    position.lon,
                    position.lat,
                )
                if (distanceToWorld > FAR_FROM_WORLD) {
                    stopNavigationUseCase.run()
                    gpsEventsRepository.insertOutsideWorldEvent()
                }
            }
        }
    }

    private suspend fun addVisitedPart(next: GpsHistoryEntity) {
        val prev = gpsRepository.getLatestPosition() ?: return
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

    private fun RectD.contains(point: GpsHistoryEntity): Boolean =
        contains(point.lon, point.lat)

    private companion object {

        const val FAR_FROM_WORLD = 2_000
    }
}
