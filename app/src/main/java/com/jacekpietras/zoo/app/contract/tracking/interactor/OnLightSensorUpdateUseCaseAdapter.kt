package com.jacekpietras.zoo.app.contract.tracking.interactor

import com.jacekpietras.zoo.domain.feature.sensors.repository.GpsRepository
import com.jacekpietras.zoo.tracking.contract.interactor.OnLightSensorUpdateUseCase

class OnLightSensorUpdateUseCaseAdapter(
    private val gpsRepository: GpsRepository,
) : OnLightSensorUpdateUseCase {

    override fun invoke(luminance: Float) {
        gpsRepository.insertLuminance(luminance)
    }
}