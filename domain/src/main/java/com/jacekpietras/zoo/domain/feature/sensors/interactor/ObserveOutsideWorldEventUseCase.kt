package com.jacekpietras.zoo.domain.feature.sensors.interactor

import com.jacekpietras.zoo.domain.feature.sensors.repository.GpsEventsRepository
import kotlinx.coroutines.flow.Flow

class ObserveOutsideWorldEventUseCase(
    private val gpsEventsRepository: GpsEventsRepository,
) {

    fun run(): Flow<Unit> =
        gpsEventsRepository.observeOutsideWorldEvents()
}