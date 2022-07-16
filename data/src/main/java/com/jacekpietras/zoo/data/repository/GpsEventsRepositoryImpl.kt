package com.jacekpietras.zoo.data.repository

import com.jacekpietras.zoo.domain.feature.sensors.repository.GpsEventsRepository
import com.jacekpietras.zoo.domain.model.Region
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow

internal class GpsEventsRepositoryImpl(
    private val outsideWorldEvents: MutableSharedFlow<Unit>,
    private val arrivalAtRegionEvents: MutableSharedFlow<Region>,
) : GpsEventsRepository {

    override suspend fun insertOutsideWorldEvent() {
        outsideWorldEvents.emit(Unit)
    }

    override fun observeOutsideWorldEvents(): Flow<Unit> =
        outsideWorldEvents

    override suspend fun insertArrivalAtRegionEvent(region: Region) {
        arrivalAtRegionEvents.emit(region)
    }

    override fun observeArrivalAtRegionEvents(): Flow<Region> =
        arrivalAtRegionEvents
}
