package com.jacekpietras.zoo.domain.feature.planner.model

import com.jacekpietras.core.PointD
import com.jacekpietras.zoo.domain.model.RegionId

data class PlanEntity(
    val planId: PlanId,
    val stages: List<Stage>,
) {

    companion object {

        val CURRENT_PLAN_ID = PlanId("currentPlanId")
    }
}

sealed class Stage {

    sealed class InRegion(
        open val regionId: RegionId,
    ) : Stage() {

        companion object {

            operator fun invoke(regionId: RegionId): InRegion = Single(regionId)
        }
    }

    data class Single(
        override val regionId: RegionId,
    ) : InRegion(regionId)

    data class Multiple(
        override val regionId: RegionId,
        val alternatives: List<RegionId>,
    ) : InRegion(regionId)

    data class InUserPosition(
        val point: PointD,
    ) : Stage()
}

class PlanId(val id: String)
