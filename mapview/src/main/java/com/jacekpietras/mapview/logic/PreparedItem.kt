package com.jacekpietras.mapview.logic

import android.graphics.Bitmap
import com.jacekpietras.geometry.PointD
import com.jacekpietras.mapview.logic.ItemVisibility.MOVED
import com.jacekpietras.mapview.model.MapDimension
import com.jacekpietras.mapview.model.PaintHolder
import com.jacekpietras.mapview.model.Pivot

internal sealed class PreparedItem<T>(
    open val minZoom: Float?,
    open var visibility: ItemVisibility,
) {

    internal sealed class PreparedColoredItem<T>(
        open val paintHolder: PaintHolder<T>,
        open val outerPaintHolder: PaintHolder<T>?,
        override val minZoom: Float?,
        override var visibility: ItemVisibility,
    ) : PreparedItem<T>(minZoom, visibility) {

        class PreparedPathItem<T>(
            val shape: DoubleArray,
            override val paintHolder: PaintHolder<T>,
            override val outerPaintHolder: PaintHolder<T>? = null,
            override val minZoom: Float? = null,
            var cacheTranslated: List<FloatArray>? = null,
            var cacheRaw: List<DoubleArray>? = null,
            override var visibility: ItemVisibility = MOVED,
        ) : PreparedColoredItem<T>(paintHolder, outerPaintHolder, minZoom, visibility)

        sealed class PreparedPolygonItem<T>(
            open val shape: DoubleArray,
            paintHolder: PaintHolder<T>,
            outerPaintHolder: PaintHolder<T>? = null,
            minZoom: Float? = null,
            open val cacheTranslated: FloatArray,
            visibility: ItemVisibility = MOVED,
        ) : PreparedColoredItem<T>(paintHolder, outerPaintHolder, minZoom, visibility) {

            class Plain<T>(
                override val shape: DoubleArray,
                override val paintHolder: PaintHolder<T>,
                override val outerPaintHolder: PaintHolder<T>? = null,
                override val minZoom: Float? = null,
                override val cacheTranslated: FloatArray,
                override var visibility: ItemVisibility = MOVED,
            ) : PreparedPolygonItem<T>(shape, paintHolder, outerPaintHolder, minZoom, cacheTranslated, visibility)

            class Block<T>(
                override val shape: DoubleArray,
                override val paintHolder: PaintHolder<T>,
                val wallPaintHolder: PaintHolder<T>,
                override val minZoom: Float? = null,
                override val cacheTranslated: FloatArray,
                val cacheRoofTranslated: FloatArray,
                override var visibility: ItemVisibility = MOVED,
            ) : PreparedPolygonItem<T>(shape, paintHolder, null, minZoom, cacheTranslated, visibility)
        }

        class PreparedCircleItem<T>(
            val point: PointD,
            val radius: MapDimension,
            override val paintHolder: PaintHolder<T>,
            override val outerPaintHolder: PaintHolder<T>? = null,
            override val minZoom: Float? = null,
            val cacheTranslated: FloatArray = FloatArray(2),
            override var visibility: ItemVisibility = MOVED,
        ) : PreparedColoredItem<T>(paintHolder, outerPaintHolder, minZoom, visibility)
    }

    class PreparedBitmapItem<T>(
        val point: PointD,
        val bitmap: Bitmap,
        override val minZoom: Float? = null,
        val cacheTranslated: FloatArray = FloatArray(2),
        override var visibility: ItemVisibility = MOVED,
        val pivot: Pivot,
    ) : PreparedItem<T>(minZoom, visibility)
}

enum class ItemVisibility {
    CACHED,
    MOVED,
}