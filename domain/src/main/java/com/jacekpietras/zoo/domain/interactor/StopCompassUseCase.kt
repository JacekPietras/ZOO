package com.jacekpietras.zoo.domain.interactor

import com.jacekpietras.zoo.domain.repository.GpsRepository

class StopCompassUseCase(
    private val gpsRepository: GpsRepository,
) {

    fun run() {
        gpsRepository.disableCompass()
    }
}