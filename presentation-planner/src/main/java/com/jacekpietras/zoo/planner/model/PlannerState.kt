package com.jacekpietras.zoo.planner.model

import com.jacekpietras.zoo.domain.model.RegionId

internal data class PlannerState(
    var plan: List<RegionId> = emptyList()
)
