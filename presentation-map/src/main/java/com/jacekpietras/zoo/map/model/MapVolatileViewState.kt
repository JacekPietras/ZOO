package com.jacekpietras.zoo.map.model

import com.jacekpietras.core.PointD
import com.jacekpietras.mapview.model.MapItem
import com.jacekpietras.zoo.core.text.RichText

internal data class MapVolatileViewState(
    val compass: Float = 0f,
    val userPosition: PointD = PointD(0.0, 0.0),
    val title: RichText = RichText.Empty,
    val content: RichText = RichText.Empty,
    val snappedPoint: PointD? = null,
    val mapData: List<MapItem> = emptyList(),
)
