package com.jacekpietras.zoo.domain.feature.planner.interactor

import com.jacekpietras.zoo.domain.feature.animal.interactor.GetAnimalUseCase
import com.jacekpietras.zoo.domain.feature.favorites.interactor.IsAnimalFavoriteUseCase
import com.jacekpietras.zoo.domain.feature.planner.model.Stage
import com.jacekpietras.zoo.domain.feature.planner.repository.PlanRepository
import com.jacekpietras.zoo.domain.interactor.GetAnimalsInRegionUseCase
import com.jacekpietras.zoo.domain.feature.animal.model.AnimalId
import com.jacekpietras.zoo.domain.model.Region
import com.jacekpietras.zoo.domain.model.RegionId

class RemoveAnimalFromCurrentPlanUseCase(
    private val planRepository: PlanRepository,
    private val getAnimalsInRegionUseCase: GetAnimalsInRegionUseCase,
    private val isAnimalFavoriteUseCase: IsAnimalFavoriteUseCase,
    private val getAnimalUseCase: GetAnimalUseCase,
    private val getOrCreateCurrentPlanUseCase: GetOrCreateCurrentPlanUseCase,
) {

    suspend fun run(animalId: AnimalId) {
        val regions = getAnimalUseCase.run(animalId).regionInZoo

        if (regions.isEmpty()) return
        if (regions.first().getFavoriteAnimals(animalId).isNotEmpty()) return

        val plan = getOrCreateCurrentPlanUseCase.run()
        val animalIsInSingleRegion = regions.size == 1

        val newStages = if (animalIsInSingleRegion) {
            plan.stages.filter { stage -> stage !is Stage.Single || stage.region.id != regions.first() }
        } else {
            plan.stages.filter { stage -> stage !is Stage.Multiple || stage.alternatives.map(Region::id) != regions }
        }
        val newPlan = plan.copy(stages = newStages)
        planRepository.setPlan(newPlan)
    }

    private suspend fun RegionId.getFavoriteAnimals(ignored: AnimalId): List<AnimalId> =
        getAnimalsInRegionUseCase.run(this)
            .filter { animal -> animal.id != ignored && isAnimalFavoriteUseCase.run(animal.id) }
            .map { it.id }
}
