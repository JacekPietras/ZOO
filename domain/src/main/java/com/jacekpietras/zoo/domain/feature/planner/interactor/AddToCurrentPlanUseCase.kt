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
        val plan = (planRepository.getPlan(CURRENT_PLAN_ID) ?: PlanEntity(CURRENT_PLAN_ID, emptyList()))
            .let {
                if (it.stages.map(Stage::regionId).contains(regionId)) {
                    it
                } else {
                    it.copy(stages = it.stages + Stage(regionId))
                }
            }
        planRepository.setPlan(plan)
    }
}
