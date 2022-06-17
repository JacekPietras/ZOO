package com.jacekpietras.zoo.domain.feature.planner.model

import com.jacekpietras.zoo.domain.model.AnimalEntity
import com.jacekpietras.zoo.domain.model.RegionId

data class PlanEntity(
    val planId: PlanId,
    val optimizationTime: Long?,
    val stages: List<Stage>,
) {

    companion object {

        val CURRENT_PLAN_ID = PlanId("currentPlanId")
    }
}

data class Stage(
    val regionId: RegionId,
    val animals: List<AnimalEntity> = emptyList(),
)

class PlanId(val id: String)
