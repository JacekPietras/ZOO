package com.jacekpietras.zoo.domain.feature.sensors.interactor

import com.jacekpietras.zoo.domain.feature.sensors.repository.GpsRepository
import kotlinx.coroutines.flow.Flow

class ObserveCompassUseCase(
    private val gpsRepository: GpsRepository,
) {

    fun run(): Flow<Float> =
        gpsRepository.observeCompass()
}