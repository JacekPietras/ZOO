package com.jacekpietras.zoo.map.model

import com.jacekpietras.core.PointD

internal data class MapVolatileState(
    val compass: Float = 0f,
    val userPosition: PointD = PointD(),
    val snappedPoint: PointD? = null,
    val shortestPath: List<PointD> = emptyList(),
)
