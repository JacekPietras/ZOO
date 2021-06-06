package com.jacekpietras.zoo.map.model

import com.jacekpietras.core.PointD
import com.jacekpietras.zoo.core.text.Text

internal data class MapVolatileViewState(
    val compass: Float,
    val userPosition: PointD,
    val title: Text,
    val content: Text,
    val snappedPoint: PointD?,
    val shortestPath: List<PointD>,
)
