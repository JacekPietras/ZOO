package com.jacekpietras.zoo.domain.interactor

import com.jacekpietras.core.PointD
import com.jacekpietras.zoo.domain.repository.GpsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GetUserPositionUseCase(
    private val gpsRepository: GpsRepository,
) {

    fun run(): Flow<PointD> =
        gpsRepository.observeLatestPosition()
            .map { PointD(it.lon, it.lat) }
}
