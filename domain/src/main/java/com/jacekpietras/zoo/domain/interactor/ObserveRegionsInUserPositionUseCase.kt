package com.jacekpietras.zoo.domain.interactor

import com.jacekpietras.core.PointD
import com.jacekpietras.zoo.domain.model.Region
import com.jacekpietras.zoo.domain.repository.GpsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

class ObserveRegionsInUserPositionUseCase(
    private val getRegionsContainingPointUseCase: GetRegionsContainingPointUseCase,
    private val gpsRepository: GpsRepository,
) {

    fun run(): Flow<List<Region>> =
        gpsRepository.observeLatestPosition()
            .map { position -> getRegionsContainingPointUseCase.run(PointD(position.lon, position.lat)) }
            .distinctUntilChanged()
}