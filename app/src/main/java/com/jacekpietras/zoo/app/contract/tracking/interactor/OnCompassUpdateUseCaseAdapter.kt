package com.jacekpietras.zoo.app.contract.tracking.interactor

import com.jacekpietras.zoo.domain.feature.sensors.interactor.InsertUserCompassUseCase
import com.jacekpietras.zoo.tracking.contract.interactor.OnCompassUpdateUseCase

class OnCompassUpdateUseCaseAdapter(
    private val insertUserCompassUseCase: InsertUserCompassUseCase
) : OnCompassUpdateUseCase {

    override fun invoke(angle: Float) {
        insertUserCompassUseCase.run(angle)
    }
}