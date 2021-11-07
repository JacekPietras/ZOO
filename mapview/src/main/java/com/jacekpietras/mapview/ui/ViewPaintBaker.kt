package com.jacekpietras.mapview.ui

import android.content.Context
import android.graphics.DashPathEffect
import android.graphics.Paint
import com.jacekpietras.mapview.model.MapDimension
import com.jacekpietras.mapview.model.MapPaint
import com.jacekpietras.mapview.model.PaintHolder

internal class ViewPaintBaker {

    fun bakeCanvasPaint(context: Context, paint: MapPaint): PaintHolder<Paint> =
        when (paint) {
            is MapPaint.DashedStroke -> paint.toCanvasPaint(context)
            is MapPaint.Fill -> paint.toCanvasPaint(context)
            is MapPaint.FillWithBorder -> paint.toCanvasPaint(context)
            is MapPaint.Stroke -> paint.toCanvasPaint(context)
            is MapPaint.StrokeWithBorder -> paint.toCanvasPaint(context)
        }

    fun bakeBorderCanvasPaint(context: Context, paint: MapPaint): PaintHolder<Paint>? =
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

    private fun MapPaint.StrokeWithBorder.toCanvasPaint(context: Context): PaintHolder<Paint> =
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

    private fun MapPaint.StrokeWithBorder.toBorderCanvasPaint(context: Context): PaintHolder<Paint> =
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

    private fun MapPaint.DashedStroke.toCanvasPaint(context: Context): PaintHolder<Paint> =
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

    private fun MapPaint.FillWithBorder.toCanvasPaint(context: Context): PaintHolder<Paint> =
        PaintHolder.Static(
            Paint().apply {
                style = Paint.Style.FILL
                isAntiAlias = true

                color = fillColor.toColorInt(context)
            }
        )

    private fun MapPaint.FillWithBorder.toBorderCanvasPaint(context: Context): PaintHolder<Paint> =
        PaintHolder.Static(
            Paint().apply {
                style = Paint.Style.FILL_AND_STROKE
                isAntiAlias = true

                color = borderColor.toColorInt(context)
                strokeWidth = borderWidth.toPixels(context) * 2
            }
        )

    private fun MapPaint.Fill.toCanvasPaint(context: Context): PaintHolder<Paint> =
        PaintHolder.Static(
            Paint().apply {
                style = Paint.Style.FILL
                isAntiAlias = true

                color = fillColor.toColorInt(context)
            }
        )
}
