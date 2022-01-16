package com.jacekpietras.zoo.data.database.mapper

import com.jacekpietras.zoo.data.database.model.PlanDto
import com.jacekpietras.zoo.domain.feature.planner.model.PlanEntity
import com.jacekpietras.zoo.domain.feature.planner.model.PlanId
import com.jacekpietras.zoo.domain.feature.planner.model.Stage
import com.jacekpietras.zoo.domain.model.RegionId

internal class PlanMapper {

    fun from(planDto: PlanDto): PlanEntity =
        PlanEntity(
            planId = planDto.planId.let(::PlanId),
            stages = planDto.stages.map { Stage(RegionId(it)) }
        )

    fun from(planEntity: PlanEntity): PlanDto =
        PlanDto(
            planId = planEntity.planId.id,
            stages = planEntity.stages.map(Stage::regionId).map(RegionId::id)
        )
}
