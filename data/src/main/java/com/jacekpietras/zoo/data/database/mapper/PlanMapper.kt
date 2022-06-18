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
                stages = stages.map { Stage.InRegion(RegionId(it)) },
            )
        }

    fun from(planEntity: PlanEntity): PlanDto =
        with(planEntity) {
            PlanDto(
                planId = planId.id,
                stages = stages.filterIsInstance<Stage.InRegion>().map { it.regionId }.map(RegionId::id),
            )
        }
}
