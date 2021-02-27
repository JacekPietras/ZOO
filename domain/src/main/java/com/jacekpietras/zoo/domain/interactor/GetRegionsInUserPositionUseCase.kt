package com.jacekpietras.zoo.domain.interactor

import com.jacekpietras.core.PointD
import com.jacekpietras.zoo.domain.repository.GpsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GetRegionsInUserPositionUseCase(
    private val getRegionsContainingPointUseCase: GetRegionsContainingPointUseCase,
    private val gpsRepository: GpsRepository,
) {

    operator fun invoke(): Flow<List<String>> =
        gpsRepository.observeLatestPosition().map { position ->
            getRegionsContainingPointUseCase(PointD(position.lon, position.lat))
        }
}