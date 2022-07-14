package com.jacekpietras.mapview.model

import com.jacekpietras.geometry.PointD

sealed class MapItem(
    open val paint: MapPaint,
    open val minZoom: Float?,
) {

    class PathMapItem(
        val path: PathD,
        override val paint: MapPaint,
        override val minZoom: Float? = null,
    ) : MapItem(paint, minZoom)

    class PolygonMapItem(
        val polygon: PolygonD,
        override val paint: MapPaint,
        override val minZoom: Float? = null,
    ) : MapItem(paint, minZoom)

    class CircleMapItem(
        val point: PointD,
        val radius: MapDimension,
        override val paint: MapPaint,
        override val minZoom: Float? = null,
    ) : MapItem(paint, minZoom)
}
