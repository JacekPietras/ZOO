package com.jacekpietras.zoo.planner.mapper

import com.jacekpietras.zoo.core.text.Text
import com.jacekpietras.zoo.domain.feature.planner.model.Stage
import com.jacekpietras.zoo.domain.model.AnimalEntity
import com.jacekpietras.zoo.domain.model.Region
import com.jacekpietras.zoo.planner.R
import com.jacekpietras.zoo.planner.model.PlannerItem
import com.jacekpietras.zoo.planner.model.PlannerState
import com.jacekpietras.zoo.planner.model.PlannerViewState

internal class PlannerStateMapper {

    fun from(state: PlannerState): PlannerViewState {
        val isExitInPlan = state.plan.any { (stage, _) -> stage is Stage.InRegion && stage.region is Region.ExitRegion }
        val list = state.plan.map { (stage, animals) ->
            when (stage) {
                is Stage.InRegion -> {
                    if (stage.region is Region.ExitRegion) {
                        PlannerItem(
                            text = Text(R.string.exit),
                            regionId = stage.region.id.id,
                        )
                    } else {
                        PlannerItem(
                            text = Text(animals.map(AnimalEntity::name).joinToString()),
                            regionId = stage.region.id.id,
                        )
                    }
                }
                is Stage.InUserPosition -> {
                    PlannerItem(
                        text = Text("not implemented"),
                        regionId = "not implemented",
                    )
                }
            }
        }

        return PlannerViewState(
            list = list,
            isEmptyViewVisible = list.isEmpty(),
            isAddExitVisible = !isExitInPlan,
        )
    }
}