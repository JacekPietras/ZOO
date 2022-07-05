package com.jacekpietras.zoo.map.model

import com.jacekpietras.geometry.PointD
import com.jacekpietras.zoo.domain.feature.map.model.MapItemEntity

internal data class MapVolatileState(
    val compass: Float = 0f,
    val userPosition: PointD = PointD(),
    val snappedPoint: PointD? = null,
    val takenRoute: List<MapItemEntity.PathEntity> = emptyList(),
    val visitedRoads: List<MapItemEntity.PathEntity> = emptyList(),
    val shortestPath: List<PointD> = emptyList(),
    val plannedPath: List<PointD> = emptyList(),
)
