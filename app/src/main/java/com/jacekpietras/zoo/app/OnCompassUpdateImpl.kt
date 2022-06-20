package com.jacekpietras.zoo.app

import com.jacekpietras.zoo.domain.feature.sensors.interactor.InsertUserCompassUseCase
import com.jacekpietras.zoo.tracking.interactor.OnCompassUpdate

class OnCompassUpdateImpl(
    private val insertUserCompassUseCase: InsertUserCompassUseCase
) : OnCompassUpdate {

    override fun invoke(angle: Float) {
        insertUserCompassUseCase.run(angle)
    }
}