package com.jacekpietras.mapview.model

import android.graphics.Bitmap
import androidx.annotation.DrawableRes
import com.jacekpietras.geometry.PointD

sealed class MapItem(
    open val minZoom: Float?,
) {

    sealed class MapColoredItem(
        open val paint: MapPaint,
        override val minZoom: Float?,
    ) : MapItem(minZoom) {

        class PathMapItem(
            val path: PathD,
            override val paint: MapPaint,
            override val minZoom: Float? = null,
        ) : MapColoredItem(paint, minZoom)

        class PolygonMapItem(
            val polygon: PolygonD,
            override val paint: MapPaint,
            override val minZoom: Float? = null,
        ) : MapColoredItem(paint, minZoom)

        class CircleMapItem(
            val point: PointD,
            val radius: MapDimension,
            override val paint: MapPaint,
            override val minZoom: Float? = null,
        ) : MapColoredItem(paint, minZoom)
    }

    class IconMapItem(
        val point: PointD,
        @DrawableRes val icon: Int,
        override val minZoom: Float? = null,
        val pivot: Pivot = Pivot.CENTER,
    ) : MapItem(minZoom)

    class BitmapMapItem(
        val point: PointD,
        val bitmap: Bitmap,
        override val minZoom: Float? = null,
        val pivot: Pivot = Pivot.BOTTOM,
    ) : MapItem(minZoom)
}
