package com.jacekpietras.zoo.planner.model

import com.jacekpietras.zoo.domain.model.RegionId

internal data class PlannerState(
    var regionUnderUnseeing: RegionId? = null,
)
