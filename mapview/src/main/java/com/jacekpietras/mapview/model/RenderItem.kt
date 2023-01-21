package com.jacekpietras.mapview.model

import android.graphics.Bitmap
import androidx.compose.runtime.Immutable
import com.jacekpietras.mapview.model.Pivot.BOTTOM
import com.jacekpietras.mapview.model.Pivot.CENTER
import com.jacekpietras.mapview.model.Pivot.LEFT
import com.jacekpietras.mapview.model.Pivot.RIGHT
import com.jacekpietras.mapview.model.Pivot.TOP

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
        class RenderBitmapItem<T>(
            override val cX: Float,
            override val cY: Float,
            val bitmap: Bitmap,
            val pivot: Pivot,
        ) : PointItem<T>(cX, cY) {

            val width: Int get() = bitmap.width
            val height: Int get() = bitmap.height

            val cXpivoted
                get() = when (pivot) {
                    TOP -> cX - width / 2
                    BOTTOM -> cX - width / 2
                    LEFT -> cX
                    RIGHT -> cX - width
                    CENTER -> cX - width / 2
                }

            val cYpivoted
                get() = when (pivot) {
                    TOP -> cY
                    BOTTOM -> cY - height
                    LEFT -> cY - height / 2
                    RIGHT -> cY - height / 2
                    CENTER -> cY - height / 2
                }
        }
    }
}