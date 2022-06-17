package com.jacekpietras.zoo.domain.feature.planner.interactor

import com.jacekpietras.core.PointD
import com.jacekpietras.zoo.domain.feature.planner.model.PlanEntity.Companion.CURRENT_PLAN_ID
import com.jacekpietras.zoo.domain.feature.planner.repository.PlanRepository
import com.jacekpietras.zoo.domain.repository.MapRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ObserveCurrentPlanPathUseCase(
    private val planRepository: PlanRepository,
    private val mapRepository: MapRepository,
) {

    // fixme it for sure can be optimized
    fun run(): Flow<List<PointD>> =
        planRepository.observePlan(CURRENT_PLAN_ID)
            .map { plan ->
                plan.stages.map { stage ->
                    mapRepository.getCurrentRegions()
                        .first { (region, _) -> region.id == stage.regionId }
                        .second
                        .findCenter()
                }
            }
}
