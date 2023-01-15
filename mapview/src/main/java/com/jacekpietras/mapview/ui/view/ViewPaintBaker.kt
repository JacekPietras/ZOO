package com.jacekpietras.mapview.ui.view

import android.content.Context
import android.graphics.DashPathEffect
import android.graphics.Paint
import com.jacekpietras.geometry.PointD
import com.jacekpietras.mapview.model.MapDimension
import com.jacekpietras.mapview.model.MapPaint
import com.jacekpietras.mapview.model.PaintHolder
import com.jacekpietras.mapview.ui.PaintBaker

internal class ViewPaintBaker(
    private val context: Context,
) : PaintBaker<Paint> {

    override fun bakeDimension(dimension: MapDimension): (zoom: Double, position: PointD, screenWidthInPixels: Int) -> Float =
        when (dimension) {
            is MapDimension.Dynamic -> { zoom, position, screenWidthInPixels ->
                dimension.toPixels(zoom, position, screenWidthInPixels)
            }
            is MapDimension.Static -> { _, _, _ ->
                dimension.toPixels(context)
            }
        }

    override fun bakeCanvasPaint(paint: MapPaint): PaintHolder<Paint> =
        when (paint) {
            is MapPaint.DashedStroke -> paint.toCanvasPaint()
            is MapPaint.Fill -> paint.toCanvasPaint()
            is MapPaint.FillWithBorder -> paint.toCanvasPaint()
            is MapPaint.Stroke -> paint.toCanvasPaint()
            is MapPaint.StrokeWithBorder -> paint.toCanvasPaint()
            is MapPaint.Circle -> paint.toCanvasPaint()
        }

    override fun bakeBorderCanvasPaint(paint: MapPaint): PaintHolder<Paint>? =
        when (paint) {
            is MapPaint.DashedStroke -> null
            is MapPaint.Fill -> null
            is MapPaint.FillWithBorder -> paint.toBorderCanvasPaint()
            is MapPaint.Stroke -> null
            is MapPaint.StrokeWithBorder -> paint.toBorderCanvasPaint()
            is MapPaint.Circle -> null
        }

    private fun MapPaint.Stroke.toCanvasPaint(): PaintHolder<Paint> =
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
            is MapDimension.Dynamic -> {
                val strokeColor = strokeColor.toColorInt(context)
                PaintHolder.Dynamic { zoom, position, screenWidthInPixels ->
                    Paint().apply {
                        style = Paint.Style.STROKE
                        isAntiAlias = true

                        color = strokeColor
                        strokeWidth = width.toPixels(zoom, position, screenWidthInPixels)
                    }
                }
            }
        }

    private fun MapPaint.StrokeWithBorder.toCanvasPaint(): PaintHolder<Paint> =
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
            is MapDimension.Dynamic -> {
                val strokeColor = strokeColor.toColorInt(context)
                PaintHolder.Dynamic { zoom, position, screenWidthInPixels ->
                    Paint().apply {
                        style = Paint.Style.STROKE
                        strokeCap = Paint.Cap.ROUND
                        isAntiAlias = true

                        color = strokeColor
                        strokeWidth = width.toPixels(zoom, position, screenWidthInPixels)
                    }
                }
            }
        }

    private fun MapPaint.StrokeWithBorder.toBorderCanvasPaint(): PaintHolder<Paint> =
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
            is MapDimension.Dynamic -> {
                val borderColor = borderColor.toColorInt(context)
                PaintHolder.Dynamic { zoom, position, screenWidthInPixels ->
                    Paint().apply {
                        style = Paint.Style.STROKE
                        strokeCap = Paint.Cap.ROUND
                        isAntiAlias = true

                        color = borderColor
                        strokeWidth = width.toPixels(zoom, position, screenWidthInPixels) +
                                2 * borderWidth.toPixels(context)
                    }
                }
            }
        }

    private fun MapPaint.DashedStroke.toCanvasPaint(): PaintHolder<Paint> =
        when (width) {
            is MapDimension.Static ->
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
            is MapDimension.Dynamic -> {
                val strokeColor = strokeColor.toColorInt(context)
                PaintHolder.Dynamic { zoom, position, screenWidthInPixels ->
                    Paint().apply {
                        style = Paint.Style.STROKE
                        isAntiAlias = true

                        color = strokeColor
                        strokeWidth = width.toPixels(zoom, position, screenWidthInPixels)
                        pathEffect = DashPathEffect(
                            floatArrayOf(
                                pattern.toPixels(context),
                                pattern.toPixels(context)
                            ), 0f
                        )
                    }
                }
            }
        }

    private fun MapPaint.FillWithBorder.toCanvasPaint(): PaintHolder<Paint> =
        PaintHolder.Static(
            Paint().apply {
                style = Paint.Style.FILL
                isAntiAlias = true

                color = fillColor.toColorInt(context)
            }
        )

    private fun MapPaint.FillWithBorder.toBorderCanvasPaint(): PaintHolder<Paint> =
        PaintHolder.Static(
            Paint().apply {
                style = Paint.Style.FILL_AND_STROKE
                isAntiAlias = true

                color = borderColor.toColorInt(context)
                strokeWidth = borderWidth.toPixels(context) * 2
            }
        )

    private fun MapPaint.Fill.toCanvasPaint(): PaintHolder<Paint> =
        PaintHolder.Static(
            Paint().apply {
                style = Paint.Style.FILL
                isAntiAlias = true

                color = fillColor.toColorInt(context)
            }
        )

    private fun MapPaint.Circle.toCanvasPaint(): PaintHolder<Paint> =
        when (radius) {
            is MapDimension.Static ->
                PaintHolder.Static(
                    Paint().apply {
                        style = Paint.Style.FILL
                        isAntiAlias = true

                        color = fillColor.toColorInt(context)
                    }
                )
            is MapDimension.Dynamic ->
                PaintHolder.Static(
                    Paint().apply {
                        style = Paint.Style.FILL
                        isAntiAlias = true

                        color = fillColor.toColorInt(context)
                    }
                )
        }
}
