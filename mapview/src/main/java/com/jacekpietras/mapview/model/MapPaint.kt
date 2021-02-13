package com.jacekpietras.mapview.model

import android.content.Context
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.util.TypedValue
import androidx.annotation.*
import androidx.core.content.ContextCompat
import com.google.android.material.color.MaterialColors

sealed class MapPaint {

    abstract fun toCanvasPaint(context: Context): Paint
    open fun toBorderCanvasPaint(context: Context): Paint? = null

    data class Stroke(
        val width: MapDimension,
        val strokeColor: MapColor,
    ) : MapPaint() {

        override fun toCanvasPaint(context: Context): Paint =
            Paint().apply {
                style = Paint.Style.STROKE
                isAntiAlias = true

                color = strokeColor.toColorInt(context)
                strokeWidth = width.toPixels(context)
            }
    }

    data class StrokeWithBorder(
        val width: MapDimension,
        val strokeColor: MapColor,
        val borderWidth: MapDimension,
        val borderColor: MapColor,
    ) : MapPaint() {

        override fun toCanvasPaint(context: Context): Paint =
            Paint().apply {
                style = Paint.Style.STROKE
                isAntiAlias = true

                color = strokeColor.toColorInt(context)
                strokeWidth = width.toPixels(context)
            }

        override fun toBorderCanvasPaint(context: Context): Paint =
            Paint().apply {
                style = Paint.Style.STROKE
                isAntiAlias = true

                color = borderColor.toColorInt(context)
                strokeWidth = width.toPixels(context) + 2 * borderWidth.toPixels(context)
            }
    }

    data class DashedStroke(
        val width: MapDimension,
        val pattern: MapDimension,
        val strokeColor: MapColor,
    ) : MapPaint() {

        override fun toCanvasPaint(context: Context): Paint =
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
    }

    data class Fill(
        val fillColor: MapColor,
    ) : MapPaint() {

        override fun toCanvasPaint(context: Context): Paint =
            Paint().apply {
                style = Paint.Style.FILL
                isAntiAlias = true

                color = fillColor.toColorInt(context)
            }
    }

    data class FillWithBorder(
        val fillColor: MapColor,
        val borderWidth: MapDimension,
        val borderColor: MapColor,
    ) : MapPaint() {

        override fun toCanvasPaint(context: Context): Paint =
            Paint().apply {
                style = Paint.Style.FILL
                isAntiAlias = true

                color = fillColor.toColorInt(context)
            }

        override fun toBorderCanvasPaint(context: Context): Paint =
            Paint().apply {
                style = Paint.Style.FILL_AND_STROKE
                isAntiAlias = true

                color = borderColor.toColorInt(context)
                strokeWidth = borderWidth.toPixels(context) * 2
            }
    }
}

sealed class MapDimension {

    abstract fun toPixels(context: Context): Float

    class Screen(
        private val dp: Int
    ) : MapDimension() {

        override fun toPixels(context: Context): Float =
            TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dp.toFloat(),
                context.resources.displayMetrics
            )
    }

    class Dimen(
        @DimenRes private val dimenRes: Int
    ) : MapDimension() {

        override fun toPixels(context: Context): Float =
            context.resources.getDimension(dimenRes)
    }

    //todo it will be hard to implement
//    class World(
//        private val meters: Double
//    ) : MapDimension() {
//
//        override fun toPixels(context: Context): Float {
//            TODO("Not yet implemented")
//        }
//    }
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
