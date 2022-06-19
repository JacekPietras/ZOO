package com.jacekpietras.zoo.data.database.mapper

import com.jacekpietras.zoo.data.database.model.PlanDto
import com.jacekpietras.zoo.data.database.model.StageDto
import com.jacekpietras.zoo.domain.feature.planner.model.PlanEntity
import com.jacekpietras.zoo.domain.feature.planner.model.PlanId
import com.jacekpietras.zoo.domain.feature.planner.model.Stage
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
                                    regionId = RegionId(regionId),
                                    mutable = mutable,
                                    alternatives = alternatives.map(::RegionId),
                                )
                            }
                            else -> {
                                Stage.Single(
                                    regionId = RegionId(regionId),
                                    mutable = mutable,
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
                                regionId = stage.regionId.id,
                                mutable = stage.mutable,
                                alternatives = stage.alternatives.map { it.id },
                            )
                        }
                        is Stage.Single -> {
                            StageDto(
                                regionId = stage.regionId.id,
                                mutable = stage.mutable,
                                alternatives = null,
                            )
                        }
                    }
                },
            )
        }
}
