package com.jacekpietras.zoo.domain.feature.planner.interactor

import com.jacekpietras.zoo.domain.feature.animal.interactor.GetAnimalUseCase
import com.jacekpietras.zoo.domain.feature.planner.model.PlanEntity
import com.jacekpietras.zoo.domain.feature.planner.model.Stage
import com.jacekpietras.zoo.domain.feature.planner.repository.PlanRepository
import com.jacekpietras.zoo.domain.model.AnimalId
import com.jacekpietras.zoo.domain.model.RegionId

class AddToCurrentPlanUseCase(
    private val planRepository: PlanRepository,
    private val getAnimalUseCase: GetAnimalUseCase,
    private val getOrCreateCurrentPlanUseCase: GetOrCreateCurrentPlanUseCase,
) {

    suspend fun run(animalId: AnimalId) {
        val plan = getOrCreateCurrentPlanUseCase.run()
        val regions = getAnimalUseCase.run(animalId).regionInZoo

        if (plan.containsRegions(regions)) return

        val newStages = plan.stages + Stage.InRegion(regions = regions)
        val newPlan = plan.copy(stages = newStages)
        planRepository.setPlan(newPlan)
    }

    private fun PlanEntity.containsRegions(
        regions: List<RegionId>,
    ) = when {
        regions.isEmpty() -> true
        regions.size == 1 -> singleRegionStages.any { it.regionId == regions.first() }
        else -> multipleRegionStages.any { it.alternatives == regions }
    }
}
