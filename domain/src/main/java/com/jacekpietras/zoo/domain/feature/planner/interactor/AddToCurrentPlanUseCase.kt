package com.jacekpietras.zoo.domain.feature.planner.interactor

import com.jacekpietras.zoo.domain.feature.planner.model.PlanEntity
import com.jacekpietras.zoo.domain.feature.planner.model.PlanEntity.Companion.CURRENT_PLAN_ID
import com.jacekpietras.zoo.domain.feature.planner.model.Stage
import com.jacekpietras.zoo.domain.feature.planner.repository.PlanRepository
import com.jacekpietras.zoo.domain.interactor.GetAnimalUseCase
import com.jacekpietras.zoo.domain.model.AnimalId

class AddToCurrentPlanUseCase(
    private val planRepository: PlanRepository,
    private val getAnimalUseCase: GetAnimalUseCase,
) {

    suspend fun run(animalId: AnimalId) {
        val animal = getAnimalUseCase.run(animalId)
        val plan = getPlan()
        val regions = animal.regionInZoo

        // fixme solve multiple regions

        if (!plan.stages.filterIsInstance<Stage.InRegion>()
                .map(Stage.InRegion::regionId).contains(regionId)
        ) {
            val newPlan = plan.copy(
                stages = plan.stages + Stage.InRegion(regionId)
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
