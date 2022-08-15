package com.jacekpietras.zoo.domain.feature.sensors.interactor

import com.jacekpietras.zoo.domain.feature.sensors.gateway.TrackingServiceGateway
import com.jacekpietras.zoo.domain.feature.sensors.repository.GpsRepository

class StartNavigationUseCase(
    private val gpsRepository: GpsRepository,
    private val trackingServiceGateway: TrackingServiceGateway,
) {

    fun run() {
        gpsRepository.enableNavigation()
        trackingServiceGateway.start()
    }
}