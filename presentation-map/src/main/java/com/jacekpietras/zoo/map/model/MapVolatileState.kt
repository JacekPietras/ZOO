package com.jacekpietras.zoo.map.model

import com.jacekpietras.geometry.PointD
import com.jacekpietras.zoo.domain.feature.map.model.MapItemEntity

internal data class MapVolatileState(
    val userPosition: PointD = PointD(),
    val snappedPoint: PointD? = null,
    val shortestPath: List<PointD> = emptyList(),
)
