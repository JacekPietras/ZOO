package com.jacekpietras.mapview.model

import android.content.Context
import android.graphics.Color
import android.util.TypedValue
import androidx.annotation.*
import androidx.core.content.ContextCompat
import com.google.android.material.color.MaterialColors
import com.jacekpietras.geometry.PointD
import com.jacekpietras.geometry.haversine
import androidx.compose.ui.graphics.Color as ComposeColor

sealed class MapPaint {

    data class Stroke(
        val width: MapDimension,
        val strokeColor: MapColor,
    ) : MapPaint()

    data class Circle(
        val radius: MapDimension,
        val fillColor: MapColor,
    ) : MapPaint()

    data class StrokeWithBorder(
        val width: MapDimension,
        val strokeColor: MapColor,
        val borderWidth: MapDimension.Static,
        val borderColor: MapColor,
    ) : MapPaint()

    data class DashedStroke(
        val width: MapDimension,
        val pattern: MapDimension.Static,
        val strokeColor: MapColor,
    ) : MapPaint()

    data class Fill(
        val fillColor: MapColor,
    ) : MapPaint()

    data class FillWithBorder(
        val fillColor: MapColor,
        val borderWidth: MapDimension.Static,
        val borderColor: MapColor,
    ) : MapPaint()
}

sealed class MapDimension {

    abstract class Static : MapDimension() {

        abstract fun toPixels(context: Context): Float

        class Screen(
            private val dp: Number
        ) : Static() {

            override fun toPixels(context: Context): Float =
                TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    dp.toFloat(),
                    context.resources.displayMetrics
                )
        }

        class Dimen(
            @DimenRes private val dimenRes: Int
        ) : Static() {

            override fun toPixels(context: Context): Float =
                context.resources.getDimension(dimenRes)
        }
    }

    abstract class Dynamic : MapDimension() {

        abstract fun toPixels(zoom: Double, position: PointD, screenWidthInPixels: Int): Float

        class World(
            internal val meters: Double
        ) : Dynamic() {

            override fun toPixels(zoom: Double, position: PointD, screenWidthInPixels: Int): Float {
                val screenWidthInMeters = haversine(
                    position.x - zoom, position.y,
                    position.x + zoom, position.y,
                )
                val screenPart = meters / screenWidthInMeters
                return (screenPart * screenWidthInPixels).toFloat()
            }
        }
    }
}

sealed class MapColor {

    @ColorInt
    abstract fun toColorInt(context: Context): Int

    class Res(
        @ColorRes private val colorRes: Int,
    ) : MapColor() {

        override fun toColorInt(context: Context): Int =
            ContextCompat.getColor(context, colorRes)
    }

    class Compose(
        @ColorRes private val color: ComposeColor,
    ) : MapColor() {

        override fun toColorInt(context: Context): Int =
            (color.value shr 32).toInt()
    }

    class Attribute(
        @AttrRes private val attrRes: Int,
    ) : MapColor() {

        override fun toColorInt(context: Context): Int =
            MaterialColors.getColor(context, attrRes, Color.MAGENTA)
    }

    class StyleAttribute(
        @AttrRes private val colorAttr: Int,
        @StyleRes private val styleAttr: Int,
    ) : MapColor() {

        override fun toColorInt(context: Context): Int {
            val attrs = intArrayOf(colorAttr)
            val ta = context.theme.obtainStyledAttributes(styleAttr, attrs)
            val textColor = ta.getColor(0, Color.MAGENTA)
            ta.recycle()
            return textColor
        }
    }

    class Hard(
        @ColorInt private val color: Int,
    ) : MapColor() {

        override fun toColorInt(context: Context): Int = color
    }
}
