package com.jacekpietras.zoo.domain.interactor

import com.jacekpietras.zoo.domain.repository.GpsRepository
import kotlinx.coroutines.flow.Flow

class ObserveCompassUseCase(
    private val gpsRepository: GpsRepository,
) {

    operator fun invoke(): Flow<Float> =
        gpsRepository.getCompass()
}