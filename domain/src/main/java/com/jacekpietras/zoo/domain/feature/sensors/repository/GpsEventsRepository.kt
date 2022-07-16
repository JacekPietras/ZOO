package com.jacekpietras.zoo.domain.feature.sensors.repository

import com.jacekpietras.zoo.domain.model.Region
import kotlinx.coroutines.flow.Flow

interface GpsEventsRepository {

    suspend fun insertOutsideWorldEvent()

    fun observeOutsideWorldEvents(): Flow<Unit>

    suspend fun insertArrivalAtRegionEvent(region: Region)

    fun observeArrivalAtRegionEvents(): Flow<Region>
}
