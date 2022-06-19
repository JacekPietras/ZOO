package com.jacekpietras.zoo.domain.feature.planner.interactor

import com.jacekpietras.zoo.domain.feature.planner.model.PlanEntity
import com.jacekpietras.zoo.domain.feature.planner.model.PlanEntity.Companion.CURRENT_PLAN_ID
import com.jacekpietras.zoo.domain.feature.planner.model.Stage
import com.jacekpietras.zoo.domain.feature.planner.repository.PlanRepository
import com.jacekpietras.zoo.domain.interactor.GetAnimalUseCase
import com.jacekpietras.zoo.domain.model.AnimalId
import com.jacekpietras.zoo.domain.model.RegionId

class AddToCurrentPlanUseCase(
    private val planRepository: PlanRepository,
    private val getAnimalUseCase: GetAnimalUseCase,
) {

    suspend fun run(animalId: AnimalId) {
        val plan = getPlan()
        val regions = getAnimalUseCase.run(animalId).regionInZoo

        if (plan.containsRegions(regions)) return

        val newPlan = plan.copy(
            stages = plan.stages + makeNewRegion(regions)
        )
        planRepository.setPlan(newPlan)
    }

    private fun makeNewRegion(regions: List<RegionId>) =
        if (regions.size == 1) {
            Stage.Single(
                regionId = regions.first(),
                mutable = true,
            )
        } else {
            Stage.Multiple(
                regionId = regions.first(),
                mutable = true,
                alternatives = regions,
            )
        }

    private fun PlanEntity.containsRegions(
        regions: List<RegionId>,
    ) = when {
        regions.isEmpty() -> true
        regions.size == 1 -> singleRegionStages.any { it.regionId == regions.first() }
        else -> multipleRegionStages.any { it.alternatives == regions }
    }

    private suspend fun getPlan(): PlanEntity =
        (planRepository.getPlan(CURRENT_PLAN_ID) ?: newPlan())

    private fun newPlan(): PlanEntity =
        PlanEntity(
            planId = CURRENT_PLAN_ID,
            stages = emptyList(),
        )
}
