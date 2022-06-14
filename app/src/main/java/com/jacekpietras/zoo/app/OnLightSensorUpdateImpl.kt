package com.jacekpietras.zoo.app

import com.jacekpietras.zoo.domain.repository.GpsRepository
import com.jacekpietras.zoo.tracking.interactor.OnLightSensorUpdate

class OnLightSensorUpdateImpl(
    private val gpsRepository: GpsRepository,
) : OnLightSensorUpdate {

    override fun invoke(luminance: Float) {
        gpsRepository.insertLuminance(luminance)
    }
}