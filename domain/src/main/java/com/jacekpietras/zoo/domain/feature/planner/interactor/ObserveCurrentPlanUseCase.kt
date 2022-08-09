package com.jacekpietras.zoo.domain.feature.planner.interactor

import com.jacekpietras.zoo.domain.feature.planner.model.PlanEntity
import com.jacekpietras.zoo.domain.feature.planner.model.PlanEntity.Companion.CURRENT_PLAN_ID
import com.jacekpietras.zoo.domain.feature.planner.repository.PlanRepository
import kotlinx.coroutines.flow.Flow

class ObserveCurrentPlanUseCase(
    private val planRepository: PlanRepository,
) {

    fun run(): Flow<PlanEntity?> =
        planRepository.observePlan(CURRENT_PLAN_ID)
}
