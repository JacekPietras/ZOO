package com.jacekpietras.zoo.domain.feature.planner.interactor

import com.jacekpietras.zoo.domain.feature.planner.model.PlanEntity
import com.jacekpietras.zoo.domain.feature.planner.model.Stage
import com.jacekpietras.zoo.domain.feature.planner.repository.PlanRepository
import com.jacekpietras.zoo.domain.model.RegionId

class MoveRegionUseCase(
    private val planRepository: PlanRepository,
    private val makeRegionImmutableUseCase: MakeRegionImmutableUseCase,
    private val getOrCreateCurrentPlanUseCase: GetOrCreateCurrentPlanUseCase,
) {

    suspend fun run(fromRegionId: RegionId, toRegionId: RegionId) {
        var plan = getOrCreateCurrentPlanUseCase.run()
        plan = makeRegionImmutableUseCase.run(plan, fromRegionId, false)

        val indexFrom = plan.indexOf(fromRegionId)
        val indexTo = plan.indexOf(toRegionId)
        val elementFrom = plan.stages[indexFrom]

        val newStages = (plan.stages - elementFrom).toMutableList().also { it.add(indexTo, elementFrom) }

        planRepository.setPlan(plan.copy(stages = newStages))
    }

    private fun PlanEntity.indexOf(regionId: RegionId): Int =
        stages.indexOfFirst { it is Stage.InRegion && it.region.id == regionId }
}