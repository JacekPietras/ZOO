package com.jacekpietras.zoo.domain.feature.vrp.model

import com.jacekpietras.geometry.PointD
import com.jacekpietras.zoo.domain.feature.planner.model.Stage

data class VrpResult(
    val stages: List<Stage>,
    val stops: List<PointD> = emptyList(),
    val path: List<PointD> = emptyList(),
)
