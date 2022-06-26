package com.jacekpietras.zoo.domain.feature.planner.interactor

import com.jacekpietras.zoo.domain.feature.planner.model.PlanEntity
import com.jacekpietras.zoo.domain.feature.planner.model.Stage
import com.jacekpietras.zoo.domain.feature.planner.repository.PlanRepository
import com.jacekpietras.zoo.domain.model.RegionId
import timber.log.Timber

class MakeRegionImmutableUseCase(
    private val planRepository: PlanRepository,
) {

    suspend fun run(regionId: RegionId, mutable: Boolean) {
        val plan = checkNotNull(planRepository.getPlan(PlanEntity.CURRENT_PLAN_ID))
        val newStages = plan.stages.map { stage ->
            if (stage is Stage.Single && stage.region.id == regionId) {
                Timber.e("dupa making region ${stage.region.id} mutable?$mutable")
                stage.copy(mutable = mutable)
            } else if (stage is Stage.Multiple && stage.region.id == regionId) {
                Timber.e("dupa making region ${stage.region.id} mutable?$mutable")
                stage.copy(mutable = mutable)
            } else {
                stage
            }
        }
        planRepository.setPlan(plan.copy(stages = newStages))
    }
}