package com.jacekpietras.zoo.domain.interactor

import com.jacekpietras.zoo.domain.business.PathListSnapper
import com.jacekpietras.zoo.domain.model.GpsHistoryEntity
import com.jacekpietras.zoo.domain.repository.GpsRepository
import com.jacekpietras.zoo.domain.repository.MapRepository

class InsertUserPositionUseCase(
    private val gpsRepository: GpsRepository,
    private val worldBoundsUseCase: GetWorldBoundsUseCase,
    private val mapRepository: MapRepository,
) {

    private var lastPosition: GpsHistoryEntity? = null
    private val pathListSnapper = PathListSnapper()

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

    private fun addVisitedPart(prev: GpsHistoryEntity?, next: GpsHistoryEntity) {
        lastPosition ?: return
        val alreadyVisited = mapRepository.getVisitedRoads()
//  todo       pathListSnapper.add(alreadyVisited, )
    }
}