package com.jacekpietras.zoo.planner.model

import com.jacekpietras.zoo.domain.feature.planner.model.Stage
import com.jacekpietras.zoo.domain.model.AnimalEntity

internal data class PlannerState(
    var plan: List<Pair<Stage, List<AnimalEntity>>> = emptyList()
)
