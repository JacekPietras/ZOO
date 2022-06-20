package com.jacekpietras.zoo.app

import com.jacekpietras.zoo.domain.feature.sensors.repository.GpsRepository
import com.jacekpietras.zoo.tracking.interactor.ObserveNavigationEnabledUseCase
import kotlinx.coroutines.flow.Flow

class ObserveNavigationEnabledUseCaseImpl(
    private val gpsRepository: GpsRepository,
) : ObserveNavigationEnabledUseCase {

    override fun run(): Flow<Boolean> =
        gpsRepository.observeNavigationEnabled()

}