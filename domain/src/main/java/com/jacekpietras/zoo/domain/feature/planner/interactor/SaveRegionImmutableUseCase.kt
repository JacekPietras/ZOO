package com.jacekpietras.zoo.domain.feature.planner.interactor

import com.jacekpietras.zoo.domain.feature.planner.repository.PlanRepository
import com.jacekpietras.zoo.domain.model.RegionId

class SaveRegionImmutableUseCase(
    private val planRepository: PlanRepository,
    private val getOrCreateCurrentPlanUseCase: GetOrCreateCurrentPlanUseCase,
    private val makeRegionImmutableUseCase: MakeRegionImmutableUseCase,
) {

    suspend fun run(regionId: RegionId, mutable: Boolean) {
        val plan = getOrCreateCurrentPlanUseCase.run()
        val newPlan = makeRegionImmutableUseCase.run(plan, regionId, mutable)
        planRepository.setPlan(newPlan)
    }
}
