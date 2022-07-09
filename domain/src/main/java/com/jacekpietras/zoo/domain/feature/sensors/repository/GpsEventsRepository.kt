package com.jacekpietras.zoo.domain.feature.sensors.repository

import kotlinx.coroutines.flow.Flow

interface GpsEventsRepository {

    suspend fun insertOutsideWorldEvent()

    fun observeOutsideWorldEvents(): Flow<Unit>
}
