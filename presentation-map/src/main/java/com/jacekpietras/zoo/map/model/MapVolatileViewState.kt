package com.jacekpietras.zoo.map.model

import com.jacekpietras.core.PointD

internal data class MapVolatileViewState(
    val compass: Float,
    val userPosition: PointD,
    val currentRegionIds: String,
    val currentAnimals: String,
    val snappedPoint: PointD?,
    val shortestPath: List<PointD>,
)
