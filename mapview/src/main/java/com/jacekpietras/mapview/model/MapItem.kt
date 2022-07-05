package com.jacekpietras.mapview.model

import com.jacekpietras.geometry.PointD

sealed class MapItem(
    open val paint: MapPaint,
) {

    class PathMapItem(
        val path: PathD,
        override val paint: MapPaint,
    ) : MapItem(paint)

    class PolygonMapItem(
        val polygon: PolygonD,
        override val paint: MapPaint,
    ) : MapItem(paint)

    class CircleMapItem(
        val point: PointD,
        val radius: MapDimension,
        override val paint: MapPaint,
    ) : MapItem(paint)
}
