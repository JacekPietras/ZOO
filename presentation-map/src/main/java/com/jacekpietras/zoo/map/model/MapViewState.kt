package com.jacekpietras.zoo.map.model

import com.jacekpietras.core.PointD
import com.jacekpietras.core.RectD
import com.jacekpietras.mapview.model.MapItem

internal class MapViewState(
    val worldBounds: RectD,
    val mapData: List<MapItem>,
    val terminalPoints: List<PointD>,
)
