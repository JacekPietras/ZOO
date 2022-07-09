package com.jacekpietras.zoo.domain.interactor

import android.text.format.DateUtils.HOUR_IN_MILLIS
import com.jacekpietras.geometry.PointD
import com.jacekpietras.zoo.domain.feature.sensors.repository.GpsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map

class ObserveUserPositionUseCase(
    private val gpsRepository: GpsRepository,
) {

    fun run(): Flow<PointD> =
        gpsRepository.observeLatestPosition()
            .filter { it.timestamp > System.currentTimeMillis() - HOUR_IN_MILLIS }
            .map { PointD(it.lon, it.lat) }
}
