package com.jacekpietras.zoo.map.model

import com.jacekpietras.zoo.domain.model.RegionId

data class PlanState(
    val distance: Double,
    val nextStageRegion: RegionId,
)
