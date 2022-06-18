package com.jacekpietras.zoo.domain.feature.planner.interactor

import com.jacekpietras.zoo.domain.feature.planner.model.PlanEntity
import com.jacekpietras.zoo.domain.feature.planner.model.PlanEntity.Companion.CURRENT_PLAN_ID
import com.jacekpietras.zoo.domain.feature.planner.model.Stage
import com.jacekpietras.zoo.domain.feature.planner.repository.PlanRepository
import com.jacekpietras.zoo.domain.model.RegionId

class AddToCurrentPlanUseCase(
    private val planRepository: PlanRepository,
) {

    suspend fun run(regionId: RegionId) {
        val plan = (planRepository.getPlan(CURRENT_PLAN_ID) ?: newPlan())
            .let {
                if (it.stages.filterIsInstance<Stage.InRegion>().map(Stage.InRegion::regionId).contains(regionId)) {
                    it
                } else {
                    it.copy(
                        optimizationTime = null,
                        stages = it.stages + Stage.InRegion(regionId)
                    )
                }
            }
        planRepository.setPlan(plan)
    }

    private fun newPlan(): PlanEntity =
        PlanEntity(
            planId = CURRENT_PLAN_ID,
            optimizationTime = null,
            stages = emptyList(),
        )
}
