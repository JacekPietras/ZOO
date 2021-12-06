package com.jacekpietras.zoo.planner.mapper

import com.jacekpietras.zoo.planner.model.PlannerItem
import com.jacekpietras.zoo.planner.model.PlannerState
import com.jacekpietras.zoo.planner.model.PlannerViewState

internal class PlannerStateMapper {

    fun from(state: PlannerState): PlannerViewState =
        PlannerViewState(
            list = listOf(PlannerItem(), PlannerItem(), PlannerItem())
        )
}