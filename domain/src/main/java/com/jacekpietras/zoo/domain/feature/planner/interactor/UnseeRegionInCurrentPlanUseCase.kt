package com.jacekpietras.zoo.domain.feature.planner.interactor

import com.jacekpietras.zoo.domain.feature.planner.model.Stage
import com.jacekpietras.zoo.domain.feature.planner.repository.PlanRepository
import com.jacekpietras.zoo.domain.model.RegionId

class UnseeRegionInCurrentPlanUseCase(
    private val planRepository: PlanRepository,
    private val getOrCreateCurrentPlanUseCase: GetOrCreateCurrentPlanUseCase,
) {

    suspend fun run(regionId: RegionId) {
        val plan = getOrCreateCurrentPlanUseCase.run()
        val newPlan = plan.copy(
            stages = plan.stages.map {
                if (it is Stage.InRegion) {
                    if (it.region.id == regionId && it.seen) {
                        it.copyWithSeen(false)
                    } else {
                        it
                    }
                } else {
                    it
                }
            }
        )
        planRepository.setPlan(newPlan)
    }
}
