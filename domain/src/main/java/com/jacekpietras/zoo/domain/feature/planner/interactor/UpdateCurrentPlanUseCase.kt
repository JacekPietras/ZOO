package com.jacekpietras.zoo.domain.feature.planner.interactor

import com.jacekpietras.zoo.domain.feature.planner.model.PlanEntity
import com.jacekpietras.zoo.domain.feature.planner.repository.PlanRepository

class UpdateCurrentPlanUseCase(
    private val planRepository: PlanRepository,
) {

    suspend fun run(plan: PlanEntity) {
        planRepository.setPlan(plan)
    }
}
