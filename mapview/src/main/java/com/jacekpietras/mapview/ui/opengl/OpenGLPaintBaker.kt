package com.jacekpietras.mapview.ui.opengl

import android.content.Context
import com.jacekpietras.geometry.PointD
import com.jacekpietras.geometry.inflateLine
import com.jacekpietras.mapview.model.MapDimension
import com.jacekpietras.mapview.model.MapPaint
import com.jacekpietras.mapview.model.OpenGLPaint
import com.jacekpietras.mapview.model.PaintHolder
import com.jacekpietras.mapview.ui.PaintBaker
import com.jacekpietras.mapview.ui.PathBaker
import com.jacekpietras.mapview.utils.colorToGLFloatArray

internal class OpenGLPaintBaker(
    private val context: Context,
) : PaintBaker<OpenGLPaint>, PathBaker {

    override fun bakeDimension(dimension: MapDimension): (zoom: Double, position: PointD, screenWidthInPixels: Int) -> Float =
        when (dimension) {
            is MapDimension.Dynamic -> { zoom, position, screenWidthInPixels ->
                dimension.toPixels(zoom, position, screenWidthInPixels)
            }
            is MapDimension.Static -> { _, _, _ ->
                dimension.toPixels(context)
            }
        }

    override fun bakePathToTriangles(paint: MapPaint, points: List<PointD>): Pair<DoubleArray?, DoubleArray?>? =
        when (paint) {
            is MapPaint.Stroke -> bakePath(points, paint.width) to null
            is MapPaint.StrokeWithBorder -> bakePath(points, paint.width) to bakePath(points, paint.borderWidth)
            else -> null
        }

    private fun bakePath(points: List<PointD>, width: MapDimension): DoubleArray? =
        when (width) {
            is MapDimension.Dynamic.World -> bakePath(points, width.meters / 2)
            is MapDimension.Static -> null
            is MapDimension.Dynamic -> null
        }

    private fun bakePath(points: List<PointD>, width: Double): DoubleArray {
        val inflated = inflateLine(points, width)
        val result = DoubleArray(inflated.size shl 1)
        val pointsCount = (inflated.size shr 1) - 1
        for (i in 0..pointsCount) {
            result[i shl 2] = inflated[i].x
            result[(i shl 2) + 1] = inflated[i].y
            result[(i shl 2) + 2] = inflated[inflated.lastIndex - i].x
            result[(i shl 2) + 3] = inflated[inflated.lastIndex - i].y
        }
        return result
    }

    override fun bakeCanvasPaint(paint: MapPaint): Pair<PaintHolder<OpenGLPaint>, PaintHolder<OpenGLPaint>?> =
        bakeInnerCanvasPaint(paint) to bakeBorderCanvasPaint(paint)

    private fun bakeInnerCanvasPaint(paint: MapPaint): PaintHolder<OpenGLPaint> =
        when (paint) {
            is MapPaint.DashedStroke -> paint.toCanvasPaint()
            is MapPaint.Fill -> paint.toCanvasPaint()
            is MapPaint.FillWithBorder -> paint.toCanvasPaint()
            is MapPaint.Stroke -> paint.toCanvasPaint()
            is MapPaint.StrokeWithBorder -> paint.toCanvasPaint()
            is MapPaint.Circle -> paint.toCanvasPaint()
        }

    private fun bakeBorderCanvasPaint(paint: MapPaint): PaintHolder<OpenGLPaint>? =
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
