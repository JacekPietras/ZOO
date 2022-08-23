package com.jacekpietras.zoo.domain.feature.sensors.interactor

import com.jacekpietras.geometry.PointD
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ObserveUserPositionUseCase(
    private val observeUserPositionWithAccuracyUseCase: ObserveUserPositionWithAccuracyUseCase,
) {

    fun run(): Flow<PointD> =
        observeUserPositionWithAccuracyUseCase.run()
            .map { PointD(it.lon, it.lat) }
}
