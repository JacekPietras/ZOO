package com.jacekpietras.mapview.ui.opengl

import android.content.Context
import com.jacekpietras.geometry.PointD
import com.jacekpietras.mapview.model.MapDimension
import com.jacekpietras.mapview.model.MapPaint
import com.jacekpietras.mapview.model.OpenGLPaint
import com.jacekpietras.mapview.model.PaintHolder
import com.jacekpietras.mapview.ui.PaintBaker
import com.jacekpietras.mapview.utils.colorToGLFloatArray

internal class OpenGLPaintBaker(
    private val context: Context,
) : PaintBaker<OpenGLPaint> {

    override fun bakeDimension(dimension: MapDimension): (zoom: Double, position: PointD, screenWidthInPixels: Int) -> Float =
        when (dimension) {
            is MapDimension.Dynamic -> { zoom, position, screenWidthInPixels ->
                dimension.toPixels(zoom, position, screenWidthInPixels)
            }
            is MapDimension.Static -> { _, _, _ ->
                dimension.toPixels(context)
            }
        }

    override fun bakeCanvasPaint(paint: MapPaint): PaintHolder<OpenGLPaint> =
        when (paint) {
            is MapPaint.DashedStroke -> paint.toCanvasPaint()
            is MapPaint.Fill -> paint.toCanvasPaint()
            is MapPaint.FillWithBorder -> paint.toCanvasPaint()
            is MapPaint.Stroke -> paint.toCanvasPaint()
            is MapPaint.StrokeWithBorder -> paint.toCanvasPaint()
            is MapPaint.Circle -> paint.toCanvasPaint()
        }

    override fun bakeBorderCanvasPaint(paint: MapPaint): PaintHolder<OpenGLPaint>? =
        when (paint) {
            is MapPaint.DashedStroke -> null
            is MapPaint.Fill -> null
            is MapPaint.FillWithBorder -> paint.toBorderCanvasPaint()
            is MapPaint.Stroke -> null
            is MapPaint.StrokeWithBorder -> paint.toBorderCanvasPaint()
            is MapPaint.Circle -> null
        }

    private fun MapPaint.Stroke.toCanvasPaint(): PaintHolder<OpenGLPaint> =
        when (width) {
            is MapDimension.Static ->
                PaintHolder.Static(
                    OpenGLPaint.Stroke(
                        color = strokeColor.toColorInt(context).colorToGLFloatArray(),
                        width = width.toPixels(context),
                    )
                )
            is MapDimension.Dynamic -> {
                val color = strokeColor.toColorInt(context).colorToGLFloatArray()
                PaintHolder.Dynamic { zoom, position, screenWidthInPixels ->
                    OpenGLPaint.Stroke(
                        color = color,
                        width = width.toPixels(zoom, position, screenWidthInPixels),
                    )
                }
            }
        }

    private fun MapPaint.StrokeWithBorder.toCanvasPaint(): PaintHolder<OpenGLPaint> =
        when (width) {
            is MapDimension.Static ->
                PaintHolder.Static(
                    OpenGLPaint.Stroke(
                        color = strokeColor.toColorInt(context).colorToGLFloatArray(),
                        width = width.toPixels(context),
                    )
                )
            is MapDimension.Dynamic -> {
                val color = strokeColor.toColorInt(context).colorToGLFloatArray()
                PaintHolder.Dynamic { zoom, position, screenWidthInPixels ->
                    OpenGLPaint.Stroke(
                        color = color,
                        width = width.toPixels(zoom, position, screenWidthInPixels),
                    )
                }
            }
        }

    private fun MapPaint.StrokeWithBorder.toBorderCanvasPaint(): PaintHolder<OpenGLPaint> =
        when (width) {
            is MapDimension.Static ->
                PaintHolder.Static(
                    OpenGLPaint.Stroke(
                        color = borderColor.toColorInt(context).colorToGLFloatArray(),
                        width = width.toPixels(context) + 2 * borderWidth.toPixels(context)
                    )
                )
            is MapDimension.Dynamic -> {
                val color = borderColor.toColorInt(context).colorToGLFloatArray()
                PaintHolder.Dynamic { zoom, position, screenWidthInPixels ->
                    OpenGLPaint.Stroke(
                        color = color,
                        width = width.toPixels(zoom, position, screenWidthInPixels) + 2 * borderWidth.toPixels(context)
                    )
                }
            }
        }

    private fun MapPaint.DashedStroke.toCanvasPaint(): PaintHolder<OpenGLPaint> =
        when (width) {
            is MapDimension.Static ->
                PaintHolder.Static(
                    OpenGLPaint.Stroke(
                        color = strokeColor.toColorInt(context).colorToGLFloatArray(),
                        width = width.toPixels(context),
                        dashed = true,
                    )
                )
            is MapDimension.Dynamic -> {
                val color = strokeColor.toColorInt(context).colorToGLFloatArray()
                PaintHolder.Dynamic { zoom, position, screenWidthInPixels ->
                    OpenGLPaint.Stroke(
                        color = color,
                        width = width.toPixels(zoom, position, screenWidthInPixels),
                        dashed = true,
                    )
                }
            }
        }

    private fun MapPaint.FillWithBorder.toCanvasPaint(): PaintHolder<OpenGLPaint> =
        PaintHolder.Static(
            OpenGLPaint.Fill(
                color = fillColor.toColorInt(context).colorToGLFloatArray()
            )
        )

    private fun MapPaint.FillWithBorder.toBorderCanvasPaint(): PaintHolder<OpenGLPaint> =
        PaintHolder.Static(
            OpenGLPaint.Stroke( // FillAndStroke
                color = borderColor.toColorInt(context).colorToGLFloatArray(),
                width = borderWidth.toPixels(context) * 2
            )
        )

    private fun MapPaint.Fill.toCanvasPaint(): PaintHolder<OpenGLPaint> =
        PaintHolder.Static(
            OpenGLPaint.Fill(
                color = fillColor.toColorInt(context).colorToGLFloatArray()
            )
        )

    private fun MapPaint.Circle.toCanvasPaint(): PaintHolder<OpenGLPaint> =
        when (radius) {
            is MapDimension.Static ->
                PaintHolder.Static(
                    OpenGLPaint.Circle(
                        color = fillColor.toColorInt(context).colorToGLFloatArray(),
                    )
                )
            is MapDimension.Dynamic -> {
                PaintHolder.Static(
                    OpenGLPaint.Circle(
                        color = fillColor.toColorInt(context).colorToGLFloatArray(),
                    )
                )
            }
        }
}