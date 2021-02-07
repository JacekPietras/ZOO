package com.jacekpietras.zoo.domain.interactor

import com.jacekpietras.zoo.domain.model.GpsHistoryEntity
import com.jacekpietras.zoo.domain.repository.GpsRepository

class InsertUserPositionUseCase(
    private val gpsRepository: GpsRepository,
) {

    suspend operator fun invoke(position: GpsHistoryEntity) {
        gpsRepository.insertPosition(position)
    }
}