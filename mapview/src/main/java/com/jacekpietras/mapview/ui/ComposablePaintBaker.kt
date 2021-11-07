package com.jacekpietras.mapview.ui

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.PaintingStyle
import androidx.compose.ui.graphics.StrokeCap
import com.jacekpietras.mapview.model.MapDimension
import com.jacekpietras.mapview.model.MapPaint
import com.jacekpietras.mapview.model.PaintHolder

internal class ComposablePaintBaker(
    private val context: Context,
) {

    fun bakeCanvasPaint(paint: MapPaint): PaintHolder<Paint> =
        when (paint) {
            is MapPaint.DashedStroke -> paint.toCanvasPaint(context)
            is MapPaint.Fill -> paint.toCanvasPaint(context)
            is MapPaint.FillWithBorder -> paint.toCanvasPaint(context)
            is MapPaint.Stroke -> paint.toCanvasPaint(context)
            is MapPaint.StrokeWithBorder -> paint.toCanvasPaint(context)
        }

    fun bakeBorderCanvasPaint(paint: MapPaint): PaintHolder<Paint>? =
        when (paint) {
            is MapPaint.DashedStroke -> null
            is MapPaint.Fill -> null
            is MapPaint.FillWithBorder -> paint.toBorderCanvasPaint(context)
            is MapPaint.Stroke -> null
            is MapPaint.StrokeWithBorder -> paint.toBorderCanvasPaint(context)
        }

    private fun MapPaint.Stroke.toCanvasPaint(context: Context): PaintHolder<Paint> =
        when (width) {
            is MapDimension.Static ->
                PaintHolder.Static(
                    Paint().apply {
                        style = PaintingStyle.Stroke
                        isAntiAlias = true

                        color = strokeColor.toColorInt(context).let(::Color)
                        strokeWidth = width.toPixels(context)
                    }
                )
            is MapDimension.Dynamic ->
                PaintHolder.Dynamic { zoom, position, screenWidthInPixels ->
                    Paint().apply {
                        style = PaintingStyle.Stroke
                        isAntiAlias = true

                        color = strokeColor.toColorInt(context).let(::Color)
                        strokeWidth = width.toPixels(zoom, position, screenWidthInPixels)
                    }
                }
        }

    private fun MapPaint.StrokeWithBorder.toCanvasPaint(context: Context): PaintHolder<Paint> =
        when (width) {
            is MapDimension.Static ->
                PaintHolder.Static(
                    Paint().apply {
                        style = PaintingStyle.Stroke
                        strokeCap = StrokeCap.Round
                        isAntiAlias = true

                        color = strokeColor.toColorInt(context).let(::Color)
                        strokeWidth = width.toPixels(context)
                    }
                )
            is MapDimension.Dynamic ->
                PaintHolder.Dynamic { zoom, position, screenWidthInPixels ->
                    Paint().apply {
                        style = PaintingStyle.Stroke
                        strokeCap = StrokeCap.Round
                        isAntiAlias = true

                        color = strokeColor.toColorInt(context).let(::Color)
                        strokeWidth = width.toPixels(zoom, position, screenWidthInPixels)
                    }
                }
        }

    private fun MapPaint.StrokeWithBorder.toBorderCanvasPaint(context: Context): PaintHolder<Paint> =
        when (width) {
            is MapDimension.Static ->
                PaintHolder.Static(
                    Paint().apply {
                        style = PaintingStyle.Stroke
                        strokeCap = StrokeCap.Round
                        isAntiAlias = true

                        color = borderColor.toColorInt(context).let(::Color)
                        strokeWidth = width.toPixels(context) +
                                2 * borderWidth.toPixels(context)
                    }
                )
            is MapDimension.Dynamic ->
                PaintHolder.Dynamic { zoom, position, screenWidthInPixels ->
                    Paint().apply {
                        style = PaintingStyle.Stroke
                        strokeCap = StrokeCap.Round
                        isAntiAlias = true

                        color = borderColor.toColorInt(context).let(::Color)
                        strokeWidth = width.toPixels(zoom, position, screenWidthInPixels) +
                                2 * borderWidth.toPixels(context)
                    }
                }
        }

    private fun MapPaint.DashedStroke.toCanvasPaint(context: Context): PaintHolder<Paint> =
        PaintHolder.Static(
            Paint().apply {
                style = PaintingStyle.Stroke
                isAntiAlias = true

                color = strokeColor.toColorInt(context).let(::Color)
                strokeWidth = width.toPixels(context)
//                pathEffect = DashPathEffect(
//                    floatArrayOf(
//                        pattern.toPixels(context),
//                        pattern.toPixels(context)
//                    ), 0f
//                )
            }
        )

    private fun MapPaint.FillWithBorder.toCanvasPaint(context: Context): PaintHolder<Paint> =
        PaintHolder.Static(
            Paint().apply {
                style = PaintingStyle.Fill
                isAntiAlias = true

                color = fillColor.toColorInt(context).let(::Color)
            }
        )

    private fun MapPaint.FillWithBorder.toBorderCanvasPaint(context: Context): PaintHolder<Paint> =
        PaintHolder.Static(
            Paint().apply {
                style = PaintingStyle.Stroke
                isAntiAlias = true

                color = borderColor.toColorInt(context).let(::Color)
                strokeWidth = borderWidth.toPixels(context) * 2
            }
        )

    private fun MapPaint.Fill.toCanvasPaint(context: Context): PaintHolder<Paint> =
        PaintHolder.Static(
            Paint().apply {
                style = PaintingStyle.Fill
                isAntiAlias = true

                color = fillColor.toColorInt(context).let(::Color)
            }
        )
}
