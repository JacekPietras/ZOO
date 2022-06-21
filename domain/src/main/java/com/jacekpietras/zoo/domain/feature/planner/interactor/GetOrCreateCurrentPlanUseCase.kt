package com.jacekpietras.zoo.domain.feature.planner.interactor

import com.jacekpietras.zoo.domain.feature.planner.model.PlanEntity
import com.jacekpietras.zoo.domain.feature.planner.model.PlanEntity.Companion.CURRENT_PLAN_ID
import com.jacekpietras.zoo.domain.feature.planner.repository.PlanRepository

class GetOrCreateCurrentPlanUseCase(
    private val planRepository: PlanRepository,
) {

    suspend fun run(): PlanEntity =
        (planRepository.getPlan(CURRENT_PLAN_ID) ?: newPlan())

    private fun newPlan(): PlanEntity =
        PlanEntity(
            planId = CURRENT_PLAN_ID,
            stages = emptyList(),
        )
}
