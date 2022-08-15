package com.jacekpietras.zoo.app.contract.tracking.interactor

import com.jacekpietras.zoo.domain.feature.sensors.repository.GpsRepository
import com.jacekpietras.zoo.tracking.contract.interactor.ObserveNavigationEnabledUseCase
import kotlinx.coroutines.flow.Flow

class ObserveNavigationEnabledUseCaseAdapter(
    private val gpsRepository: GpsRepository,
) : ObserveNavigationEnabledUseCase {

    override fun run(): Flow<Boolean> =
        gpsRepository.observeNavigationEnabled()
}
