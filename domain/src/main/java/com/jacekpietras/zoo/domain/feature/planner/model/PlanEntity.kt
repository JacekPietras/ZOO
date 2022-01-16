package com.jacekpietras.zoo.domain.feature.planner.model

import com.jacekpietras.zoo.domain.model.RegionId

data class PlanEntity(
    val planId: PlanId,
    val regions: List<RegionId>,
) {

    companion object {

        val CURRENT_PLAN_ID = PlanId("currentPlanId")
    }
}

class PlanId(val id: String)
