package com.jacekpietras.zoo.domain.feature.sensors.interactor

import com.jacekpietras.zoo.domain.feature.sensors.model.GpsHistoryEntity

interface InsertUserPositionUseCase {

    fun run(position: GpsHistoryEntity)
}
