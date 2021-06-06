package com.jacekpietras.zoo.app

import com.jacekpietras.zoo.domain.interactor.InsertUserPositionUseCase
import com.jacekpietras.zoo.domain.model.GpsHistoryEntity
import com.jacekpietras.zoo.tracking.OnLocationUpdate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class OnLocationUpdateImpl(
    private val insertUserPositionUseCase: InsertUserPositionUseCase
) : OnLocationUpdate {

    override fun invoke(time: Long, lat: Double, lon: Double) {
        CoroutineScope(Dispatchers.IO).launch {
            insertUserPositionUseCase.run(GpsHistoryEntity(time, lat, lon))
        }
    }
}