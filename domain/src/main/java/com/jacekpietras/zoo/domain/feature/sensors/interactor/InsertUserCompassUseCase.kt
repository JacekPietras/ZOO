package com.jacekpietras.zoo.domain.feature.sensors.interactor

import com.jacekpietras.zoo.domain.feature.sensors.repository.GpsRepository

class InsertUserCompassUseCase(
    private val gpsRepository: GpsRepository,
) {

    fun run(angle: Float) {
        gpsRepository.insertCompass(angle)
    }
}