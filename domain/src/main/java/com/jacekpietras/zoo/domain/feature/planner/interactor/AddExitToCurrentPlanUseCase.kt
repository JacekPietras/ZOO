package com.jacekpietras.zoo.domain.feature.planner.interactor

import com.jacekpietras.zoo.domain.interactor.FindRegionUseCase
import com.jacekpietras.zoo.domain.model.Region

class AddExitToCurrentPlanUseCase(
    private val addStageToCurrentPlanUseCase: AddStageToCurrentPlanUseCase,
    private val findRegionUseCase: FindRegionUseCase,
) {

    suspend fun run() {
        val regions = findRegionUseCase.run { it is Region.ExitRegion }
        addStageToCurrentPlanUseCase.run(regions, mutable = false)
    }
}
