package com.jacekpietras.zoo.map.model

import com.jacekpietras.core.PointD
import com.jacekpietras.zoo.domain.model.AnimalEntity
import com.jacekpietras.zoo.domain.model.Region

internal data class MapState(
    val compass: Float = 0f,
    val userPosition: PointD = PointD(),
    val regionsInUserPosition: List<String> = emptyList(),
    val regionsWithAnimalsInUserPosition: List<Pair<Region, List<AnimalEntity>>> = emptyList(),
    val animalsInUserPosition: List<AnimalEntity> = emptyList(),
    val snappedPoint: PointD? = null,
    val shortestPath: List<PointD> = emptyList(),
    val isToolbarOpened: Boolean = false,
    val toolbarMode: MapToolbarMode? = null,
)
