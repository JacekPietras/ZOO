package com.jacekpietras.zoo.map.model

import com.jacekpietras.core.PointD
import com.jacekpietras.zoo.core.text.Text

internal data class MapViewState(
    val compass: Float = 0f,
    val userPosition: PointD = PointD(0.0, 0.0),
    val snappedPoint: PointD? = null,
    val shortestPath: List<PointD> = emptyList(),

    val isBackArrowShown: Boolean = false,
    val isGuidanceShown: Boolean = false,
    val title: Text = Text.Empty,
    val content: Text = Text.Empty,
    val mapCarouselItems: List<MapCarouselItem> = emptyList(),
    val isMapActionsVisible: Boolean = false,
    val mapActions: List<MapAction> = emptyList(),
)
