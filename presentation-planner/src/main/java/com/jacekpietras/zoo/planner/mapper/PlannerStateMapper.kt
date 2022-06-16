package com.jacekpietras.zoo.planner.mapper

import com.jacekpietras.zoo.core.text.Text
import com.jacekpietras.zoo.planner.model.PlannerItem
import com.jacekpietras.zoo.planner.model.PlannerState
import com.jacekpietras.zoo.planner.model.PlannerViewState

internal class PlannerStateMapper {

    fun from(state: PlannerState): PlannerViewState =
        PlannerViewState(
            list = state.plan.map {
                PlannerItem(
                    text = Text(it)
                )
            }
        )
}