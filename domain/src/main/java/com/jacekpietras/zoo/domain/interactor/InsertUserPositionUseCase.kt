package com.jacekpietras.zoo.domain.interactor

import com.jacekpietras.zoo.domain.model.GpsHistoryEntity

interface InsertUserPositionUseCase {

    fun run(position: GpsHistoryEntity)
}
