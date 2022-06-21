package com.jacekpietras.zoo.domain.feature.planner.interactor

import com.jacekpietras.zoo.domain.feature.planner.model.Stage
import com.jacekpietras.zoo.domain.feature.planner.repository.PlanRepository

class RemoveRegionFromCurrentPlanUseCase(
    private val planRepository: PlanRepository,
    private val getOrCreateCurrentPlanUseCase: GetOrCreateCurrentPlanUseCase,
) {

    suspend fun run(stage: Stage) {
        val plan = getOrCreateCurrentPlanUseCase.run()
        val newPlan = plan.copy(
            stages = plan.stages - stage
        )
        planRepository.setPlan(newPlan)
    }
}
