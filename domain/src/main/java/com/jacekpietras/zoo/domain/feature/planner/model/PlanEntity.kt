package com.jacekpietras.zoo.domain.feature.planner.model

import com.jacekpietras.core.PointD
import com.jacekpietras.zoo.domain.model.RegionId

data class PlanEntity(
    val planId: PlanId,
    val stages: List<Stage>,
) {

    val regionStages get() = stages.filterIsInstance<Stage.InRegion>()
    val singleRegionStages get() = stages.filterIsInstance<Stage.Single>()
    val multipleRegionStages get() = stages.filterIsInstance<Stage.Multiple>()

    companion object {

        val CURRENT_PLAN_ID = PlanId("currentPlanId")
    }
}

sealed class Stage {

    sealed class InRegion(
        open val regionId: RegionId,
        open val mutable: Boolean,
    ) : Stage() {

        companion object {

            operator fun invoke(regionId: RegionId, mutable: Boolean = true): InRegion =
                Single(regionId, mutable)
        }
    }

    data class Single(
        override val regionId: RegionId,
        override val mutable: Boolean,
    ) : InRegion(regionId, mutable)

    data class Multiple(
        override val regionId: RegionId,
        override val mutable: Boolean,
        val alternatives: List<RegionId>,
    ) : InRegion(regionId, mutable)

    data class InUserPosition(
        val point: PointD,
    ) : Stage()
}

class PlanId(val id: String)
