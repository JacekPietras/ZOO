package com.jacekpietras.zoo.domain.model

import com.jacekpietras.core.PointD

data class VisitedRoadPoint(
    val point: PointD,
    val visited: List<Pair<Float, Float>>
)
