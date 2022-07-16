package com.jacekpietras.zoo.data.database.mapper

import com.jacekpietras.zoo.data.database.model.PlanDto
import com.jacekpietras.zoo.data.database.model.StageDto
import com.jacekpietras.zoo.data.database.model.StageRegionType
import com.jacekpietras.zoo.domain.feature.planner.model.PlanEntity
import com.jacekpietras.zoo.domain.feature.planner.model.PlanId
import com.jacekpietras.zoo.domain.feature.planner.model.Stage
import com.jacekpietras.zoo.domain.model.Region
import com.jacekpietras.zoo.domain.model.Region.AnimalRegion
import com.jacekpietras.zoo.domain.model.RegionId

internal class PlanMapper {

    fun from(planDto: PlanDto): PlanEntity =
        with(planDto) {
            PlanEntity(
                planId = planId.let(::PlanId),
                stages = stages.map { stage ->
                    with(stage) {
                        when {
                            alternatives?.isNotEmpty() == true -> {
                                Stage.Multiple(
                                    region = makeRegion(regionType, regionId),
                                    mutable = mutable,
                                    seen = seen,
                                    alternatives = alternatives.map { makeRegion(regionType, it) },
                                )
                            }
                            else -> {
                                Stage.Single(
                                    region = makeRegion(regionType, regionId),
                                    mutable = mutable,
                                    seen = seen,
                                )
                            }
                        }
                    }
                }
            )
        }

    fun from(planEntity: PlanEntity): PlanDto =
        with(planEntity) {
            PlanDto(
                planId = planId.id,
                stages = stages.mapNotNull { stage ->
                    when (stage) {
                        is Stage.InUserPosition -> {
                            null
                        }
                        is Stage.Multiple -> {
                            StageDto(
                                regionId = stage.region.id.id,
                                mutable = stage.mutable,
                                seen = stage.seen,
                                alternatives = stage.alternatives.map { it.id.id },
                                regionType = stage.toRegionType(),
                            )
                        }
                        is Stage.Single -> {
                            StageDto(
                                regionId = stage.region.id.id,
                                mutable = stage.mutable,
                                seen = stage.seen,
                                alternatives = null,
                                regionType = stage.toRegionType(),
                            )
                        }
                    }
                },
            )
        }

    private fun Stage.InRegion.toRegionType() =
        when (region) {
            is AnimalRegion -> StageRegionType.ANIMAL
            is Region.ExitRegion -> StageRegionType.EXIT
            is Region.RestaurantRegion -> StageRegionType.RESTAURANT
            is Region.WcRegion -> StageRegionType.WC
        }

    private fun makeRegion(type: StageRegionType, regionId: String) =
        when (type) {
            StageRegionType.ANIMAL -> AnimalRegion(RegionId(regionId))
            StageRegionType.EXIT -> Region.ExitRegion(RegionId(regionId))
            StageRegionType.RESTAURANT -> Region.RestaurantRegion(RegionId(regionId))
            StageRegionType.WC -> Region.WcRegion(RegionId(regionId))
        }
}
