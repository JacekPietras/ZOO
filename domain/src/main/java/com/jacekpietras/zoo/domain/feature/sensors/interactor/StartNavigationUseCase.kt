package com.jacekpietras.zoo.domain.feature.sensors.interactor

import com.jacekpietras.zoo.domain.feature.sensors.repository.GpsRepository

class StartNavigationUseCase(
    private val gpsRepository: GpsRepository,
) {

    fun run() {
        gpsRepository.enableNavigation()
    }
}