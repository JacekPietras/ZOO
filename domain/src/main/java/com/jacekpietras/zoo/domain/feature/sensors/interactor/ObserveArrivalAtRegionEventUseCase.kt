package com.jacekpietras.zoo.domain.feature.sensors.interactor

import com.jacekpietras.zoo.domain.feature.sensors.repository.GpsEventsRepository
import com.jacekpietras.zoo.domain.model.Region
import kotlinx.coroutines.flow.Flow

class ObserveArrivalAtRegionEventUseCase(
    private val gpsEventsRepository: GpsEventsRepository,
) {

    fun run(): Flow<Region> =
        gpsEventsRepository.observeArrivalAtRegionEvents()
}