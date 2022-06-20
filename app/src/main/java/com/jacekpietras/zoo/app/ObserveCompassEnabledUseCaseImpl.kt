package com.jacekpietras.zoo.app

import com.jacekpietras.zoo.domain.feature.sensors.repository.GpsRepository
import com.jacekpietras.zoo.tracking.interactor.ObserveCompassEnabledUseCase
import kotlinx.coroutines.flow.Flow

class ObserveCompassEnabledUseCaseImpl(
    private val gpsRepository: GpsRepository,
) : ObserveCompassEnabledUseCase {

    override fun run(): Flow<Boolean> =
        gpsRepository.observeCompassEnabled()

}