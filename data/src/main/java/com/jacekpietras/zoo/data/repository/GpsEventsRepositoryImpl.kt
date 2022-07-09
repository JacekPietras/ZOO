package com.jacekpietras.zoo.data.repository

import com.jacekpietras.zoo.domain.feature.sensors.repository.GpsEventsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow

internal class GpsEventsRepositoryImpl : GpsEventsRepository {

    private val outsideWorldEvents = MutableSharedFlow<Unit>()

    override suspend fun insertOutsideWorldEvent() {
        outsideWorldEvents.emit(Unit)
    }

    override fun observeOutsideWorldEvents(): Flow<Unit> =
        outsideWorldEvents
}
