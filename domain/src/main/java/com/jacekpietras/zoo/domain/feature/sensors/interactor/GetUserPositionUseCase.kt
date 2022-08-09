package com.jacekpietras.zoo.domain.feature.sensors.interactor

import android.text.format.DateUtils.HOUR_IN_MILLIS
import com.jacekpietras.geometry.PointD
import com.jacekpietras.zoo.domain.feature.sensors.repository.GpsRepository

class GetUserPositionUseCase(
    private val gpsRepository: GpsRepository,
) {

    suspend fun run(): PointD? =
        gpsRepository.getLatestPosition()
            ?.takeIf { it.timestamp > System.currentTimeMillis() - HOUR_IN_MILLIS }
            ?.let { PointD(it.lon, it.lat) }
}
