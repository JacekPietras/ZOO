package com.jacekpietras.zoo.map.model

import com.jacekpietras.geometry.PointD

internal data class MapVolatileState(
    val userPosition: PointD = PointD(),
    val userPositionAccuracy: Float = 0f,
    val snappedPoint: PointD? = null,
    val shortestPath: List<PointD> = emptyList(),
)
