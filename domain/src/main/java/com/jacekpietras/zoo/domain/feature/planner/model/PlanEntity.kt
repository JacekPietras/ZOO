package com.jacekpietras.zoo.domain.feature.planner.model

import com.jacekpietras.core.PointD
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

sealed class Stage {

    data class InRegion(
        val regionId: RegionId,
    ) : Stage()

    data class InUserPosition(
        val point: PointD,
    ) : Stage()
}

class PlanId(val id: String)
