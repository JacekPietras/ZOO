package com.jacekpietras.zoo.domain.feature.planner.interactor

import com.jacekpietras.zoo.domain.feature.planner.model.PlanEntity
import com.jacekpietras.zoo.domain.feature.planner.model.Stage
import com.jacekpietras.zoo.domain.feature.planner.repository.PlanRepository
import com.jacekpietras.zoo.domain.model.RegionId

class MakeRegionImmutableUseCase(
    private val planRepository: PlanRepository,
) {

    suspend fun run(regionId: RegionId, mutable:Boolean) {
        val plan = checkNotNull(planRepository.getPlan(PlanEntity.CURRENT_PLAN_ID))
        val newStages = plan.stages.map { stage ->
            // fixme it shouldn't be only for single
            if (stage is Stage.Single && stage.region.id == regionId) {
                stage.copy(mutable = mutable)
            } else {
                stage
            }
        }
        planRepository.setPlan(plan.copy(stages = newStages))
    }
}