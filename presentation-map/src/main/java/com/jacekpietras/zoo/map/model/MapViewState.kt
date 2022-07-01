package com.jacekpietras.zoo.map.model

import com.jacekpietras.core.PointD
import com.jacekpietras.zoo.core.text.RichText

internal data class MapViewState(
    val compass: Float = 0f,
    val isNightThemeSuggested: Boolean = false,
    val userPosition: PointD = PointD(0.0, 0.0),
    val snappedPoint: PointD? = null,
    val shortestPath: List<PointD> = emptyList(),

    val isBackArrowShown: Boolean = false,
    val isGuidanceShown: Boolean = false,
    val title: RichText = RichText.Empty,
    @Deprecated("used only in old fragment")
    val content: RichText = RichText.Empty,
    val mapCarouselItems: List<MapCarouselItem> = emptyList(),
    val isMapActionsVisible: Boolean = false,
    val mapActions: List<MapAction> = emptyList(),
)
