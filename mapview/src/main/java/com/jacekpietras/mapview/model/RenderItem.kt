package com.jacekpietras.mapview.model

import android.graphics.Bitmap
import androidx.annotation.DrawableRes
import androidx.compose.runtime.Immutable

sealed class RenderItem<T> {

    @Immutable
    class RenderPathItem<T>(
        val shape: FloatArray,
        val paint: T,
    ) : RenderItem<T>()

    @Immutable
    class RenderPolygonItem<T>(
        val shape: FloatArray,
        val paint: T,
    ) : RenderItem<T>()

    sealed class PointItem<T>(
        open val cX: Float,
        open val cY: Float,
    ) : RenderItem<T>() {

        @Immutable
        class RenderCircleItem<T>(
            override val cX: Float,
            override val cY: Float,
            val radius: Float,
            val paint: T,
        ) : PointItem<T>(cX, cY)

        @Immutable
        class RenderIconItem<T>(
            override val cX: Float,
            override val cY: Float,
            @DrawableRes val iconRes: Int,
            val pivot: Pivot,
        ) : PointItem<T>(cX, cY) {

            val width: Int get() = 24
            val height: Int get() = 24
        }

        @Immutable
        class RenderBitmapItem<T>(
            override val cX: Float,
            override val cY: Float,
            val bitmap: Bitmap,
            val pivot: Pivot,
        ) : PointItem<T>(cX, cY) {

            val width: Int get() = bitmap.width
            val height: Int get() = bitmap.height
        }
    }
}