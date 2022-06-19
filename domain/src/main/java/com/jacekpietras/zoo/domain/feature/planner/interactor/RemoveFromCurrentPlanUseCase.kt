package com.jacekpietras.zoo.domain.feature.planner.interactor

import com.jacekpietras.zoo.domain.feature.favorites.interactor.IsAnimalFavoriteUseCase
import com.jacekpietras.zoo.domain.feature.planner.model.PlanEntity
import com.jacekpietras.zoo.domain.feature.planner.model.PlanEntity.Companion.CURRENT_PLAN_ID
import com.jacekpietras.zoo.domain.feature.planner.model.Stage
import com.jacekpietras.zoo.domain.feature.planner.repository.PlanRepository
import com.jacekpietras.zoo.domain.interactor.GetAnimalUseCase
import com.jacekpietras.zoo.domain.interactor.GetAnimalsInRegionUseCase
import com.jacekpietras.zoo.domain.model.AnimalId

class RemoveFromCurrentPlanUseCase(
    private val planRepository: PlanRepository,
    private val getAnimalsInRegionUseCase: GetAnimalsInRegionUseCase,
    private val isAnimalFavoriteUseCase: IsAnimalFavoriteUseCase,
    private val getAnimalUseCase: GetAnimalUseCase,
) {

    suspend fun run(animalId: AnimalId) {
        val animal = getAnimalUseCase.run(animalId)
        val plan = getPlan()
        val regions = animal.regionInZoo

        val plannedAnimalsInRegion = getAnimalsInRegionUseCase.run(regionId)
            .filter { animal -> isAnimalFavoriteUseCase.run(animal.id) }
        if (plannedAnimalsInRegion.isEmpty()) {
            val newPlan = plan.copy(
                stages = plan.stages.fil - Stage.InRegion(regionId)
            )
            planRepository.setPlan(newPlan)
        }

    }

    private suspend fun getPlan(): PlanEntity =
        (planRepository.getPlan(CURRENT_PLAN_ID) ?: newPlan())

    private fun newPlan(): PlanEntity =
        PlanEntity(
            planId = CURRENT_PLAN_ID,
            stages = emptyList(),
        )
}
