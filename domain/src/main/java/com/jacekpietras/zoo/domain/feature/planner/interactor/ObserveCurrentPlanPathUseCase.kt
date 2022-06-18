package com.jacekpietras.zoo.domain.feature.planner.interactor

import com.jacekpietras.core.PointD
import com.jacekpietras.zoo.domain.feature.pathfinder.intractor.CalculateShortestPathUseCase
import com.jacekpietras.zoo.domain.feature.planner.model.PlanEntity.Companion.CURRENT_PLAN_ID
import com.jacekpietras.zoo.domain.feature.planner.repository.PlanRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ObserveCurrentPlanPathUseCase(
    private val planRepository: PlanRepository,
    private val calculateShortestPathUseCase: CalculateShortestPathUseCase,
) {

    fun run(): Flow<List<PointD>> =
        planRepository.observePlan(CURRENT_PLAN_ID)
            .map { plan ->
                plan.stages
                    .map { stage -> stage.regionId }
                    .let { regions -> calculateShortestPathUseCase.run(regions) }
                    .map { it.second }
                    .flatten()
            }
}
