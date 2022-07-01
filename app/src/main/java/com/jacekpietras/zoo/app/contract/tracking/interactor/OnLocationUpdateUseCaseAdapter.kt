package com.jacekpietras.zoo.app.contract.tracking.interactor

import com.jacekpietras.zoo.domain.feature.sensors.model.GpsHistoryEntity
import com.jacekpietras.zoo.domain.interactor.InsertUserPositionUseCase
import com.jacekpietras.zoo.tracking.contract.interactor.OnLocationUpdateUseCase

class OnLocationUpdateUseCaseAdapter(
    private val insertUserPositionUseCase: InsertUserPositionUseCase
) : OnLocationUpdateUseCase {

    override fun invoke(time: Long, lat: Double, lon: Double) {
        insertUserPositionUseCase.run(GpsHistoryEntity(time, lat, lon))
    }
}