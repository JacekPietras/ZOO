package com.jacekpietras.zoo.data.database.mapper

import com.jacekpietras.zoo.data.database.model.PlanDto
import com.jacekpietras.zoo.domain.feature.planner.model.PlanEntity
import com.jacekpietras.zoo.domain.feature.planner.model.PlanId
import com.jacekpietras.zoo.domain.feature.planner.model.Stage
import com.jacekpietras.zoo.domain.model.RegionId

internal class PlanMapper {

    fun from(planDto: PlanDto): PlanEntity =
        with(planDto) {
            PlanEntity(
                planId = planId.let(::PlanId),
                optimizationTime = optimizationTime,
                stages = stages.map { Stage(RegionId(it)) },
            )
        }

    fun from(planEntity: PlanEntity): PlanDto =
        with(planEntity) {
            PlanDto(
                planId = planId.id,
                optimizationTime = optimizationTime,
                stages = stages.map(Stage::regionId).map(RegionId::id),
            )
        }
}
