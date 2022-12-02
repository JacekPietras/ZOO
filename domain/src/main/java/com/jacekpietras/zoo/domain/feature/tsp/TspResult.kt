package com.jacekpietras.zoo.domain.feature.tsp

import com.jacekpietras.geometry.PointD
import com.jacekpietras.zoo.domain.feature.planner.model.Stage

data class TspResult(
    val stages: List<Stage>,
    val stops: List<PointD> = emptyList(),
    val path: List<PointD> = emptyList(),
)
