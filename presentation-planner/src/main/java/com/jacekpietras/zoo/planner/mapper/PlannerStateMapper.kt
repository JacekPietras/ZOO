package com.jacekpietras.zoo.planner.mapper

import com.jacekpietras.zoo.core.text.RichText
import com.jacekpietras.zoo.domain.feature.planner.model.Stage
import com.jacekpietras.zoo.domain.model.AnimalEntity
import com.jacekpietras.zoo.domain.model.Region
import com.jacekpietras.zoo.planner.R
import com.jacekpietras.zoo.planner.model.PlannerItem
import com.jacekpietras.zoo.planner.model.PlannerViewState

internal class PlannerStateMapper {

    fun from(plan: List<Pair<Stage, List<AnimalEntity>>>?): PlannerViewState {
        val isExitInPlan = plan?.any { (stage, _) -> stage is Stage.InRegion && stage.region is Region.ExitRegion } ?: false
        val list = plan?.map { (stage, animals) ->
            when (stage) {
                is Stage.InRegion -> {
                    if (stage.region is Region.ExitRegion) {
                        PlannerItem(
                            text = RichText(R.string.exit),
                            regionId = stage.region.id.id,
                            isMutable = true,
                            isFixed = true,
                        )
                    } else {
                        PlannerItem(
                            text = RichText(animals.map(AnimalEntity::name).joinToString()),
                            regionId = stage.region.id.id,
                            isMultiple = stage is Stage.Multiple,
                            isMutable = stage.mutable,
                        )
                    }
                }
                is Stage.InUserPosition -> {
                    PlannerItem(
                        text = RichText("not implemented"),
                        regionId = "not implemented",
                        isFixed = true,
                        isMutable = true,
                    )
                }
            }
        } ?: emptyList()

        return PlannerViewState(
            list = list,
            isEmptyViewVisible = list.isEmpty(),
            isAddExitVisible = !isExitInPlan && list.isNotEmpty(),
        )
    }
}