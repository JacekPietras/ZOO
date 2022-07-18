package com.jacekpietras.mapview.ui

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect.Companion.dashPathEffect
import androidx.compose.ui.graphics.StrokeCap
import com.jacekpietras.geometry.PointD
import com.jacekpietras.mapview.model.ComposablePaint
import com.jacekpietras.mapview.model.MapDimension
import com.jacekpietras.mapview.model.MapPaint
import com.jacekpietras.mapview.model.PaintHolder

class ComposablePaintBaker(
    private val context: Context,
) : PaintBaker<ComposablePaint> {

    override fun bakeDimension(dimension: MapDimension): (zoom: Double, position: PointD, screenWidthInPixels: Int) -> Float =
        when (dimension) {
            is MapDimension.Dynamic -> { zoom, position, screenWidthInPixels ->
                dimension.toPixels(zoom, position, screenWidthInPixels)
            }
            is MapDimension.Static -> { _, _, _ ->
                dimension.toPixels(context)
            }
        }

    override fun bakeCanvasPaint(paint: MapPaint): PaintHolder<ComposablePaint> =
        when (paint) {
            is MapPaint.DashedStroke -> paint.toCanvasPaint()
            is MapPaint.Fill -> paint.toCanvasPaint()
            is MapPaint.FillWithBorder -> paint.toCanvasPaint()
            is MapPaint.Stroke -> paint.toCanvasPaint()
            is MapPaint.StrokeWithBorder -> paint.toCanvasPaint()
            is MapPaint.Circle -> paint.toCanvasPaint()
        }

    override fun bakeBorderCanvasPaint(paint: MapPaint): PaintHolder<ComposablePaint>? =
        when (paint) {
            is MapPaint.DashedStroke -> null
            is MapPaint.Fill -> null
            is MapPaint.FillWithBorder -> paint.toBorderCanvasPaint()
            is MapPaint.Stroke -> null
            is MapPaint.StrokeWithBorder -> paint.toBorderCanvasPaint()
            is MapPaint.Circle -> null
        }

    private fun MapPaint.Stroke.toCanvasPaint(): PaintHolder<ComposablePaint> =
        when (width) {
            is MapDimension.Static ->
                PaintHolder.Static(
                    ComposablePaint.Stroke(
                        color = strokeColor.toColorInt(context).let(::Color),
                        width = width.toPixels(context),
                    )
                )
            is MapDimension.Dynamic -> {
                val color = strokeColor.toColorInt(context).let(::Color)
                PaintHolder.Dynamic { zoom, position, screenWidthInPixels ->
                    ComposablePaint.Stroke(
                        color = color,
                        width = width.toPixels(zoom, position, screenWidthInPixels),
                    )
                }
            }
        }

    private fun MapPaint.StrokeWithBorder.toCanvasPaint(): PaintHolder<ComposablePaint> =
        when (width) {
            is MapDimension.Static ->
                PaintHolder.Static(
                    ComposablePaint.Stroke(
                        cap = StrokeCap.Round,
                        color = strokeColor.toColorInt(context).let(::Color),
                        width = width.toPixels(context),
                    )
                )
            is MapDimension.Dynamic -> {
                val color = strokeColor.toColorInt(context).let(::Color)
                PaintHolder.Dynamic { zoom, position, screenWidthInPixels ->
                    ComposablePaint.Stroke(
                        cap = StrokeCap.Round,
                        color = color,
                        width = width.toPixels(zoom, position, screenWidthInPixels),
                    )
                }
            }
        }

    private fun MapPaint.StrokeWithBorder.toBorderCanvasPaint(): PaintHolder<ComposablePaint> =
        when (width) {
            is MapDimension.Static ->
                PaintHolder.Static(
                    ComposablePaint.Stroke(
                        cap = StrokeCap.Round,
                        color = borderColor.toColorInt(context).let(::Color),
                        width = width.toPixels(context) + 2 * borderWidth.toPixels(context)
                    )
                )
            is MapDimension.Dynamic -> {
                val color = borderColor.toColorInt(context).let(::Color)
                PaintHolder.Dynamic { zoom, position, screenWidthInPixels ->
                    ComposablePaint.Stroke(
                        cap = StrokeCap.Round,
                        color = color,
                        width = width.toPixels(zoom, position, screenWidthInPixels) + 2 * borderWidth.toPixels(context)
                    )
                }
            }
        }

    private fun MapPaint.DashedStroke.toCanvasPaint(): PaintHolder<ComposablePaint> =
        when (width) {
            is MapDimension.Static ->
                PaintHolder.Static(
                    ComposablePaint.Stroke(
                        color = strokeColor.toColorInt(context).let(::Color),
                        width = width.toPixels(context),
                        pathEffect = dashPathEffect(
                            floatArrayOf(
                                pattern.toPixels(context),
                                pattern.toPixels(context),
                            ),
                            phase = 0f,
                        )
                    )
                )
            is MapDimension.Dynamic -> {
                val color = strokeColor.toColorInt(context).let(::Color)
                PaintHolder.Dynamic { zoom, position, screenWidthInPixels ->
                    ComposablePaint.Stroke(
                        color = color,
                        width = width.toPixels(zoom, position, screenWidthInPixels),
                        pathEffect = dashPathEffect(
                            floatArrayOf(
                                pattern.toPixels(context),
                                pattern.toPixels(context),
                            ),
                            phase = 0f,
                        )
                    )
                }
            }
        }

    private fun MapPaint.FillWithBorder.toCanvasPaint(): PaintHolder<ComposablePaint> =
        PaintHolder.Static(
            ComposablePaint.Fill(
                color = fillColor.toColorInt(context).let(::Color)
            )
        )

    private fun MapPaint.FillWithBorder.toBorderCanvasPaint(): PaintHolder<ComposablePaint> =
        PaintHolder.Static(
            ComposablePaint.Stroke( // FillAndStroke
                color = borderColor.toColorInt(context).let(::Color),
                width = borderWidth.toPixels(context) * 2
            )
        )

    private fun MapPaint.Fill.toCanvasPaint(): PaintHolder<ComposablePaint> =
        PaintHolder.Static(
            ComposablePaint.Fill(
                color = fillColor.toColorInt(context).let(::Color)
            )
        )

    private fun MapPaint.Circle.toCanvasPaint(): PaintHolder<ComposablePaint> =
        when (radius) {
            is MapDimension.Static ->
                PaintHolder.Static(
                    ComposablePaint.Circle(
                        color = fillColor.toColorInt(context).let(::Color),
                    )
                )
            is MapDimension.Dynamic -> {
                PaintHolder.Static(
                    ComposablePaint.Circle(
                        color = fillColor.toColorInt(context).let(::Color),
                    )
                )
            }
        }
}
