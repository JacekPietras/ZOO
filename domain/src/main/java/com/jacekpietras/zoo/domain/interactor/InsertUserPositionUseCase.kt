package com.jacekpietras.zoo.domain.interactor

import com.jacekpietras.zoo.domain.model.GpsHistoryEntity
import com.jacekpietras.zoo.domain.repository.GpsRepository

class InsertUserPositionUseCase(
    private val gpsRepository: GpsRepository,
    private val worldBoundsUseCase: GetWorldBoundsUseCase,
) {

    suspend operator fun invoke(position: GpsHistoryEntity) {
        val bounds = worldBoundsUseCase()
        if (bounds.contains(position.lon, position.lat)) {
            gpsRepository.insertPosition(position)
        }
    }
}