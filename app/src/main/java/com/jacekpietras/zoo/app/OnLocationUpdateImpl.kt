package com.jacekpietras.zoo.app

import com.jacekpietras.zoo.domain.feature.sensors.model.GpsHistoryEntity
import com.jacekpietras.zoo.domain.interactor.InsertUserPositionUseCase
import com.jacekpietras.zoo.tracking.interactor.OnLocationUpdate

class OnLocationUpdateImpl(
    private val insertUserPositionUseCase: InsertUserPositionUseCase
) : OnLocationUpdate {

    override fun invoke(time: Long, lat: Double, lon: Double) {
        insertUserPositionUseCase.run(GpsHistoryEntity(time, lat, lon))
    }
}