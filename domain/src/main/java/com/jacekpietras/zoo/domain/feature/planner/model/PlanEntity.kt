package com.jacekpietras.zoo.domain.feature.planner.model

import com.jacekpietras.geometry.PointD
import com.jacekpietras.zoo.domain.model.Region

data class PlanEntity(
    val planId: PlanId,
    val stages: List<Stage>,
) {

    val singleRegionStages get() = stages.filterIsInstance<Stage.Single>()
    val multipleRegionStages get() = stages.filterIsInstance<Stage.Multiple>()

    companion object {

        val CURRENT_PLAN_ID = PlanId("currentPlanId")
    }
}

sealed class Stage {

    sealed class InRegion(
        open val region: Region,
        open val mutable: Boolean,
    ) : Stage() {

        companion object {

            operator fun invoke(region: Region, mutable: Boolean = true): InRegion =
                Single(region, mutable)

            operator fun invoke(regions: List<Region>, mutable: Boolean = true) =
                when {
                    regions.isEmpty() -> {
                        throw IllegalStateException("trying to add stage with no regions")
                    }
                    regions.size == 1 -> {
                        Single(
                            region = regions.first(),
                            mutable = mutable,
                        )
                    }
                    else -> {
                        Multiple(
                            region   = regions.first(),
                            mutable = mutable,
                            alternatives = regions,
                        )
                    }
                }
        }
    }

    data class Single(
        override val region: Region,
        override val mutable: Boolean,
    ) : InRegion(region, mutable)

    data class Multiple(
        override val region: Region,
        override val mutable: Boolean,
        val alternatives: List<Region>,
    ) : InRegion(region, mutable)

    data class InUserPosition(
        val point: PointD,
    ) : Stage()
}

class PlanId(val id: String)
