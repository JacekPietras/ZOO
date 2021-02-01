package com.jacekpietras.zoo.map.model

import android.content.Context
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.util.TypedValue
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.DimenRes
import androidx.core.content.ContextCompat

internal sealed class MapPaint {

    abstract fun toCanvasPaint(context: Context): Paint

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
}

internal sealed class MapDimension {

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

    class World(
        private val meters: Double
    ) : MapDimension() {

        override fun toPixels(context: Context): Float {
            TODO("Not yet implemented")
        }
    }
}

internal sealed class MapColor {

    @ColorInt
    abstract fun toColorInt(context: Context): Int

    class Res(
        @ColorRes private val colorRes: Int
    ) : MapColor() {

        override fun toColorInt(context: Context): Int =
            ContextCompat.getColor(context, colorRes)
    }

    class Hard(
        @ColorInt private val color: Int
    ) : MapColor() {

        override fun toColorInt(context: Context): Int = color
    }
}