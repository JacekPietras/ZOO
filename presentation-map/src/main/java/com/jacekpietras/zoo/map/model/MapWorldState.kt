package com.jacekpietras.zoo.map.model

import com.jacekpietras.core.PointD
import com.jacekpietras.core.RectD
import com.jacekpietras.zoo.domain.model.MapItemEntity.PathEntity
import com.jacekpietras.zoo.domain.model.MapItemEntity.PolygonEntity

internal data class MapWorldState(
    val worldBounds: RectD = RectD(),
    val buildings: List<PolygonEntity> = emptyList(),
    val aviary: List<PolygonEntity> = emptyList(),
    val roads: List<PathEntity> = emptyList(),
    val lines: List<PathEntity> = emptyList(),
    val technicalRoute: List<PathEntity> = emptyList(),
    val oldTakenRoute: List<PathEntity> = emptyList(),
    val terminalPoints: List<PointD> = emptyList(),
)
