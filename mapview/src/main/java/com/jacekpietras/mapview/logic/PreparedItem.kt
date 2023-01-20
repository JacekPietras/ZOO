package com.jacekpietras.mapview.logic

import android.graphics.Bitmap
import androidx.annotation.DrawableRes
import com.jacekpietras.geometry.PointD
import com.jacekpietras.mapview.logic.ItemVisibility.HIDDEN
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
            override var visibility: ItemVisibility = HIDDEN,
        ) : PreparedColoredItem<T>(paintHolder, outerPaintHolder, minZoom, visibility)

        class PreparedPolygonItem<T>(
            val shape: DoubleArray,
            override val paintHolder: PaintHolder<T>,
            override val outerPaintHolder: PaintHolder<T>? = null,
            override val minZoom: Float? = null,
            var cacheTranslated: FloatArray? = null,
            override var visibility: ItemVisibility = HIDDEN,
        ) : PreparedColoredItem<T>(paintHolder, outerPaintHolder, minZoom, visibility)

        class PreparedCircleItem<T>(
            val point: PointD,
            val radius: MapDimension,
            override val paintHolder: PaintHolder<T>,
            override val outerPaintHolder: PaintHolder<T>? = null,
            override val minZoom: Float? = null,
            val cacheTranslated: FloatArray = FloatArray(2),
            override var visibility: ItemVisibility = HIDDEN,
        ) : PreparedColoredItem<T>(paintHolder, outerPaintHolder, minZoom, visibility)
    }

    class PreparedIconItem<T>(
        val point: PointD,
        @DrawableRes val icon: Int,
        override val minZoom: Float? = null,
        val cacheTranslated: FloatArray = FloatArray(2),
        override var visibility: ItemVisibility = HIDDEN,
        val pivot: Pivot,
    ) : PreparedItem<T>(minZoom, visibility)

    class PreparedBitmapItem<T>(
        val point: PointD,
        val bitmap: Bitmap,
        override val minZoom: Float? = null,
        val cacheTranslated: FloatArray = FloatArray(2),
        override var visibility: ItemVisibility = HIDDEN,
        val pivot: Pivot,
    ) : PreparedItem<T>(minZoom, visibility)
}

enum class ItemVisibility {
    CACHED,
    VISIBLE,
    HIDDEN,
}