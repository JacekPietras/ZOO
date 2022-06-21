package com.jacekpietras.zoo.domain.feature.planner.interactor

import com.jacekpietras.zoo.domain.feature.planner.model.Stage
import com.jacekpietras.zoo.domain.feature.planner.repository.PlanRepository
import com.jacekpietras.zoo.domain.model.RegionId

class AddStageToCurrentPlanUseCase(
    private val planRepository: PlanRepository,
    private val getOrCreateCurrentPlanUseCase: GetOrCreateCurrentPlanUseCase,
) {

    suspend fun run(vararg regions: RegionId) {
        val plan = getOrCreateCurrentPlanUseCase.run()
        val newPlan = plan.copy(
            stages = plan.stages + Stage.InRegion(regions = regions.toList())
        )
        planRepository.setPlan(newPlan)
    }
}
