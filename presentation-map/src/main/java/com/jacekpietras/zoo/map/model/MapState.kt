package com.jacekpietras.zoo.map.model

import com.jacekpietras.core.PointD
import com.jacekpietras.zoo.domain.model.AnimalEntity

internal data class MapState(
    val compass: Float = 0f,
    val userPosition: PointD = PointD(),
    val regionsInUserPosition: List<String> = emptyList(),
    val animalsInUserPosition: List<AnimalEntity> = emptyList(),
    val snappedPoint: PointD? = null,
    val shortestPath: List<PointD> = emptyList(),
    val selectedAnimal: AnimalEntity? = null,
)
