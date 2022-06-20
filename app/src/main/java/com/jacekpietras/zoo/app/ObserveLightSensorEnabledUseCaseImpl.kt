package com.jacekpietras.zoo.app

import com.jacekpietras.zoo.domain.feature.sensors.repository.GpsRepository
import com.jacekpietras.zoo.tracking.interactor.ObserveLightSensorEnabledUseCase
import kotlinx.coroutines.flow.Flow

class ObserveLightSensorEnabledUseCaseImpl(
    private val gpsRepository: GpsRepository,
) : ObserveLightSensorEnabledUseCase {

    override fun run(): Flow<Boolean> =
        gpsRepository.observeLightSensorEnabled()
}
