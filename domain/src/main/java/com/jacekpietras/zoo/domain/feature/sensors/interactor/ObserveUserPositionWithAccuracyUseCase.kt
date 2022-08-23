package com.jacekpietras.zoo.domain.feature.sensors.interactor

import android.text.format.DateUtils.HOUR_IN_MILLIS
import com.jacekpietras.zoo.domain.feature.sensors.model.GpsHistoryEntity
import com.jacekpietras.zoo.domain.feature.sensors.repository.GpsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter

class ObserveUserPositionWithAccuracyUseCase(
    private val gpsRepository: GpsRepository,
) {

    fun run(): Flow<GpsHistoryEntity> =
        gpsRepository.observeLatestPosition()
            .filter { it.timestamp > System.currentTimeMillis() - HOUR_IN_MILLIS }
}
