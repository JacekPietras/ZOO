package com.jacekpietras.zoo.domain.feature.planner.interactor

import com.jacekpietras.zoo.domain.feature.favorites.interactor.IsAnimalFavoriteUseCase
import com.jacekpietras.zoo.domain.feature.planner.model.PlanEntity
import com.jacekpietras.zoo.domain.feature.planner.model.PlanEntity.Companion.CURRENT_PLAN_ID
import com.jacekpietras.zoo.domain.feature.planner.model.Stage
import com.jacekpietras.zoo.domain.feature.planner.repository.PlanRepository
import com.jacekpietras.zoo.domain.interactor.GetAnimalUseCase
import com.jacekpietras.zoo.domain.interactor.GetAnimalsInRegionUseCase
import com.jacekpietras.zoo.domain.model.AnimalId
import com.jacekpietras.zoo.domain.model.RegionId

class RemoveFromCurrentPlanUseCase(
    private val planRepository: PlanRepository,
    private val getAnimalsInRegionUseCase: GetAnimalsInRegionUseCase,
    private val isAnimalFavoriteUseCase: IsAnimalFavoriteUseCase,
    private val getAnimalUseCase: GetAnimalUseCase,
) {

    suspend fun run(animalId: AnimalId) {
        val plan = getPlan()
        val regions = getAnimalUseCase.run(animalId).regionInZoo

        if (regions.isEmpty()) return

        if (regions.first().getFavoriteAnimals(animalId).isNotEmpty()) return

        if (regions.size == 1) {
            val newPlan = plan.copy(
                stages = plan.stages
                    .filter { stage -> stage !is Stage.Single || stage.regionId != regions.first() }
            )
            planRepository.setPlan(newPlan)

        } else if (regions.size > 1) {
            val newPlan = plan.copy(
                stages = plan.stages
                    .filter { stage -> stage !is Stage.Multiple || stage.alternatives != regions }
            )
            planRepository.setPlan(newPlan)
        }
    }

    suspend fun run(stage: Stage) {
        val plan = getPlan()
        val newPlan = plan.copy(
            stages = plan.stages - stage
        )
        planRepository.setPlan(newPlan)
    }

    private suspend fun RegionId.getFavoriteAnimals(ignored: AnimalId): List<AnimalId> =
        getAnimalsInRegionUseCase.run(this)
            .filter { animal -> animal.id != ignored && isAnimalFavoriteUseCase.run(animal.id) }
            .map { it.id }

    private suspend fun getPlan(): PlanEntity =
        (planRepository.getPlan(CURRENT_PLAN_ID) ?: newPlan())

    private fun newPlan(): PlanEntity =
        PlanEntity(
            planId = CURRENT_PLAN_ID,
            stages = emptyList(),
        )
}
