package com.jacekpietras.zoo.app.contract.tracking.interactor

import com.jacekpietras.zoo.domain.feature.sensors.repository.GpsRepository
import com.jacekpietras.zoo.tracking.contract.interactor.ObserveCompassEnabledUseCase
import kotlinx.coroutines.flow.Flow

class ObserveCompassEnabledUseCaseAdapter(
    private val gpsRepository: GpsRepository,
) : ObserveCompassEnabledUseCase {

    override fun run(): Flow<Boolean> =
        gpsRepository.observeCompassEnabled()

}