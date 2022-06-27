package com.jacekpietras.zoo.domain.feature.planner.interactor

import com.jacekpietras.zoo.domain.feature.planner.model.PlanEntity
import com.jacekpietras.zoo.domain.feature.planner.model.Stage
import com.jacekpietras.zoo.domain.model.RegionId

class MakeRegionImmutableUseCase() {

    fun run(plan: PlanEntity, regionId: RegionId, mutable: Boolean): PlanEntity {
        val newStages = plan.stages.map { stage ->
            if (stage is Stage.Single && stage.region.id == regionId) {
                stage.copy(mutable = mutable)
            } else if (stage is Stage.Multiple && stage.region.id == regionId) {
                stage.copy(mutable = mutable)
            } else {
                stage
            }
        }
        return plan.copy(stages = newStages)
    }
}