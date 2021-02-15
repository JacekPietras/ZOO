package com.jacekpietras.mapview.model

import android.content.Context
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.util.TypedValue
import androidx.annotation.*
import androidx.core.content.ContextCompat
import com.google.android.material.color.MaterialColors
import com.jacekpietras.core.PointD
import com.jacekpietras.core.haversine

sealed class MapPaint {

    internal abstract fun toCanvasPaint(context: Context): PaintHolder
    internal open fun toBorderCanvasPaint(context: Context): PaintHolder? = null

    data class Stroke(
        val width: MapDimension,
        val strokeColor: MapColor,
    ) : MapPaint() {

        override fun toCanvasPaint(context: Context): PaintHolder =
            when (width) {
                is MapDimension.Static ->
                    PaintHolder.Static(
                        Paint().apply {
                            style = Paint.Style.STROKE
                            isAntiAlias = true

                            color = strokeColor.toColorInt(context)
                            strokeWidth = width.toPixels(context)
                        }
                    )
                is MapDimension.Dynamic ->
                    PaintHolder.Dynamic { zoom, position, screenWidthInPixels ->
                        Paint().apply {
                            style = Paint.Style.STROKE
                            isAntiAlias = true

                            color = strokeColor.toColorInt(context)
                            strokeWidth = width.toPixels(zoom, position, screenWidthInPixels)
                        }
                    }
            }
    }

    data class StrokeWithBorder(
        val width: MapDimension,
        val strokeColor: MapColor,
        val borderWidth: MapDimension.Static,
        val borderColor: MapColor,
    ) : MapPaint() {

        override fun toCanvasPaint(context: Context): PaintHolder =
            when (width) {
                is MapDimension.Static ->
                    PaintHolder.Static(
                        Paint().apply {
                            style = Paint.Style.STROKE
                            strokeCap = Paint.Cap.ROUND
                            isAntiAlias = true

                            color = strokeColor.toColorInt(context)
                            strokeWidth = width.toPixels(context)
                        }
                    )
                is MapDimension.Dynamic ->
                    PaintHolder.Dynamic { zoom, position, screenWidthInPixels ->
                        Paint().apply {
                            style = Paint.Style.STROKE
                            strokeCap = Paint.Cap.ROUND
                            isAntiAlias = true

                            color = strokeColor.toColorInt(context)
                            strokeWidth = width.toPixels(zoom, position, screenWidthInPixels)
                        }
                    }
            }

        override fun toBorderCanvasPaint(context: Context): PaintHolder =
            when (width) {
                is MapDimension.Static ->
                    PaintHolder.Static(
                        Paint().apply {
                            style = Paint.Style.STROKE
                            strokeCap = Paint.Cap.ROUND
                            isAntiAlias = true

                            color = borderColor.toColorInt(context)
                            strokeWidth = width.toPixels(context) +
                                    2 * borderWidth.toPixels(context)
                        }
                    )
                is MapDimension.Dynamic ->
                    PaintHolder.Dynamic { zoom, position, screenWidthInPixels ->
                        Paint().apply {
                            style = Paint.Style.STROKE
                            strokeCap = Paint.Cap.ROUND
                            isAntiAlias = true

                            color = borderColor.toColorInt(context)
                            strokeWidth = width.toPixels(zoom, position, screenWidthInPixels) +
                                    2 * borderWidth.toPixels(context)
                        }
                    }
            }
    }

    data class DashedStroke(
        val width: MapDimension.Static,
        val pattern: MapDimension.Static,
        val strokeColor: MapColor,
    ) : MapPaint() {

        override fun toCanvasPaint(context: Context): PaintHolder =
            PaintHolder.Static(
                Paint().apply {
                    style = Paint.Style.STROKE
                    isAntiAlias = true

                    color = strokeColor.toColorInt(context)
                    strokeWidth = width.toPixels(context)
                    pathEffect = DashPathEffect(
                        floatArrayOf(
                            pattern.toPixels(context),
                            pattern.toPixels(context)
                        ), 0f
                    )
                }
            )
    }

    data class Fill(
        val fillColor: MapColor,
    ) : MapPaint() {

        override fun toCanvasPaint(context: Context): PaintHolder =
            PaintHolder.Static(
                Paint().apply {
                    style = Paint.Style.FILL
                    isAntiAlias = true

                    color = fillColor.toColorInt(context)
                }
            )
    }

    data class FillWithBorder(
        val fillColor: MapColor,
        val borderWidth: MapDimension.Static,
        val borderColor: MapColor,
    ) : MapPaint() {

        override fun toCanvasPaint(context: Context): PaintHolder =
            PaintHolder.Static(
                Paint().apply {
                    style = Paint.Style.FILL
                    isAntiAlias = true

                    color = fillColor.toColorInt(context)
                }
            )

        override fun toBorderCanvasPaint(context: Context): PaintHolder =
            PaintHolder.Static(
                Paint().apply {
                    style = Paint.Style.FILL_AND_STROKE
                    isAntiAlias = true

                    color = borderColor.toColorInt(context)
                    strokeWidth = borderWidth.toPixels(context) * 2
                }
            )
    }
}

sealed class MapDimension {

    abstract class Static : MapDimension() {

        abstract fun toPixels(context: Context): Float

        class Screen(
            private val dp: Number
        ) : MapDimension.Static() {

            override fun toPixels(context: Context): Float =
                TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    dp.toFloat(),
                    context.resources.displayMetrics
                )
        }

        class Dimen(
            @DimenRes private val dimenRes: Int
        ) : MapDimension.Static() {

            override fun toPixels(context: Context): Float =
                context.resources.getDimension(dimenRes)
        }
    }

    abstract class Dynamic : MapDimension() {

        abstract fun toPixels(zoom: Double, position: PointD, screenWidthInPixels: Int): Float

        class World(
            private val meters: Double
        ) : MapDimension.Dynamic() {

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
