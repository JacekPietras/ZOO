package com.jacekpietras.mapview.logic

import android.graphics.Bitmap
import androidx.annotation.DrawableRes
import com.jacekpietras.geometry.PointD
import com.jacekpietras.mapview.model.MapDimension
import com.jacekpietras.mapview.model.PaintHolder

internal sealed class PreparedItem<T>(
    open val minZoom: Float?,
) {

    internal sealed class PreparedColoredItem<T>(
        open val paintHolder: PaintHolder<T>,
        open val outerPaintHolder: PaintHolder<T>?,
        override val minZoom: Float?,
    ) : PreparedItem<T>(minZoom) {

        class PreparedPathItem<T>(
            val shape: DoubleArray,
            override val paintHolder: PaintHolder<T>,
            override val outerPaintHolder: PaintHolder<T>? = null,
            override val minZoom: Float? = null,
        ) : PreparedColoredItem<T>(paintHolder, outerPaintHolder, minZoom)

        class PreparedPolygonItem<T>(
            val shape: DoubleArray,
            override val paintHolder: PaintHolder<T>,
            override val outerPaintHolder: PaintHolder<T>? = null,
            override val minZoom: Float? = null,
        ) : PreparedColoredItem<T>(paintHolder, outerPaintHolder, minZoom)

        class PreparedCircleItem<T>(
            val point: PointD,
            val radius: MapDimension,
            override val paintHolder: PaintHolder<T>,
            override val outerPaintHolder: PaintHolder<T>? = null,
            override val minZoom: Float? = null,
        ) : PreparedColoredItem<T>(paintHolder, outerPaintHolder, minZoom)
    }

    class PreparedIconItem<T>(
        val point: PointD,
        @DrawableRes val icon: Int,
        override val minZoom: Float? = null,
    ) : PreparedItem<T>(minZoom)

    class PreparedBitmapItem<T>(
        val point: PointD,
        val bitmap: Bitmap,
        override val minZoom: Float? = null,
    ) : PreparedItem<T>(minZoom)
}
