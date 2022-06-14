package com.jacekpietras.zoo.domain.interactor

import com.jacekpietras.zoo.domain.repository.GpsRepository

class InsertUserCompassUseCase(
    private val gpsRepository: GpsRepository,
) {

    fun run(angle: Float) {
        gpsRepository.insertCompass(angle)
    }
}