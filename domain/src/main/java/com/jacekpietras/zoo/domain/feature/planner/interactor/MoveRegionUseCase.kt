package com.jacekpietras.zoo.domain.feature.planner.interactor

import com.jacekpietras.zoo.domain.feature.planner.model.PlanEntity
import com.jacekpietras.zoo.domain.feature.planner.model.Stage
import com.jacekpietras.zoo.domain.feature.planner.repository.PlanRepository
import com.jacekpietras.zoo.domain.model.RegionId

class MoveRegionUseCase(
    private val planRepository: PlanRepository,
    private val makeRegionImmutableUseCase: MakeRegionImmutableUseCase,
) {

    suspend fun run(fromRegionId: RegionId, toRegionId: RegionId) {
        makeRegionImmutableUseCase.run(fromRegionId, false)

        val plan = checkNotNull(planRepository.getPlan(PlanEntity.CURRENT_PLAN_ID))

        val indexFrom = plan.indexOf(fromRegionId)
        val indexTo = plan.indexOf(toRegionId)
        val elementFrom = plan.stages[indexFrom]

        val newStages = (plan.stages - elementFrom).toMutableList().also { it.add(indexTo, elementFrom) }

        planRepository.setPlan(plan.copy(stages = newStages))
    }

    private fun PlanEntity.indexOf(regionId: RegionId): Int =
        stages.indexOfFirst { it is Stage.InRegion && it.region.id == regionId }
}