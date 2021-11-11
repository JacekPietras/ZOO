package com.jacekpietras.zoo.domain.interactor

import com.jacekpietras.zoo.domain.repository.GpsRepository

class StartCompassUseCase(
    private val gpsRepository: GpsRepository,
) {

    fun run() {
        gpsRepository.enableCompass()
    }
}