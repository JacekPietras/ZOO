package com.jacekpietras.zoo.domain.feature.planner.interactor

import com.jacekpietras.zoo.domain.feature.favorites.interactor.IsAnimalFavoriteUseCase
import com.jacekpietras.zoo.domain.feature.planner.model.PlanEntity
import com.jacekpietras.zoo.domain.feature.planner.model.PlanEntity.Companion.CURRENT_PLAN_ID
import com.jacekpietras.zoo.domain.feature.planner.model.Stage
import com.jacekpietras.zoo.domain.feature.planner.repository.PlanRepository
import com.jacekpietras.zoo.domain.interactor.GetAnimalsInRegionUseCase
import com.jacekpietras.zoo.domain.model.RegionId

class RemoveFromCurrentPlanUseCase(
    private val planRepository: PlanRepository,
    private val getAnimalsInRegionUseCase: GetAnimalsInRegionUseCase,
    private val isAnimalFavoriteUseCase: IsAnimalFavoriteUseCase,
) {

    suspend fun run(regionId: RegionId) {
        val plan = (planRepository.getPlan(CURRENT_PLAN_ID) ?: newPlan())
            .let {
                val plannedAnimalsInRegion = getAnimalsInRegionUseCase.run(regionId)
                    .filter { animal -> isAnimalFavoriteUseCase.run(animal.id) }
                if (plannedAnimalsInRegion.isEmpty()) {
                    it.copy(
                        stages = it.stages - Stage.InRegion(regionId)
                    )
                } else {
                    it
                }
            }
        planRepository.setPlan(plan)
    }

    private fun newPlan(): PlanEntity =
        PlanEntity(
            planId = CURRENT_PLAN_ID,
            stages = emptyList(),
        )
}
