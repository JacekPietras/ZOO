package com.jacekpietras.zoo.planner.mapper

import com.jacekpietras.zoo.core.text.Text
import com.jacekpietras.zoo.domain.feature.planner.model.Stage
import com.jacekpietras.zoo.domain.model.AnimalEntity
import com.jacekpietras.zoo.planner.model.PlannerItem
import com.jacekpietras.zoo.planner.model.PlannerState
import com.jacekpietras.zoo.planner.model.PlannerViewState

internal class PlannerStateMapper {

    fun from(state: PlannerState): PlannerViewState =
        PlannerViewState(
            list = state.plan.map { (stage, animals) ->
                when (stage) {
                    is Stage.InRegion -> {
                        PlannerItem(
                            text = Text(animals.map(AnimalEntity::name).joinToString()),
                            regionId = stage.regionId.id,
                        )
                    }
                    is Stage.InUserPosition -> {
                        PlannerItem(
                            text = Text("not implemented"),
                            regionId = "not implemented",
                        )
                    }
                }
            }
        )
}