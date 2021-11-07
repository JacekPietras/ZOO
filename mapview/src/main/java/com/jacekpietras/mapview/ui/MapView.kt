package com.jacekpietras.mapview.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.util.AttributeSet
import com.jacekpietras.core.PointD
import com.jacekpietras.core.RectD
import com.jacekpietras.mapview.R
import com.jacekpietras.mapview.model.*
import com.jacekpietras.mapview.utils.doAnimation
import com.jacekpietras.mapview.utils.drawPath

class MapView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : GesturedView(context, attrs, defStyleAttr) {

    var setOnPointPlacedListener: ((PointD) -> Unit)?
        get() = mapData.setOnPointPlacedListener
        set(value) {
            mapData.setOnPointPlacedListener = value
        }
    var shortestPath: List<PointD>
        get() = mapData.shortestPath
        set(value) {
            mapData.shortestPath = value
        }
    var clickOnWorld: PointD?
        get() = mapData.clickOnWorld
        set(value) {
            mapData.clickOnWorld = value
        }
    var compass: Float
        get() = mapData.compass
        set(value) {
            mapData.compass = value
        }
    var userPosition: PointD?
        get() = mapData.userPosition
        set(value) {
            mapData.userPosition = value
        }
    var terminalPoints: List<PointD>
        get() = mapData.terminalPoints
        set(value) {
            mapData.terminalPoints = value
        }
    var objectList: List<MapItem<Paint>>
        get() = mapData.objectList
        set(value) {
            mapData.objectList = value
        }
    var worldBounds: RectD
        get() = mapData.worldBounds
        set(value) {
            mapData.worldBounds = value
        }

    private val mapData = MapViewLogic(
        getCurrentHeight = { height },
        getCurrentWidth = { width },
        doAnimation = { lambda -> doAnimation(true, lambda) },
        invalidate = { invalidate() },
        bakeCanvasPaint = this::bakeCanvasPaint,
        bakeBorderCanvasPaint = this::bakeBorderCanvasPaint,
    )

    private fun bakeCanvasPaint(paint: MapPaint): PaintHolder<Paint> =
        when (paint) {
            is MapPaint.DashedStroke -> paint.toCanvasPaint(context)
            is MapPaint.Fill -> paint.toCanvasPaint(context)
            is MapPaint.FillWithBorder -> paint.toCanvasPaint(context)
            is MapPaint.Stroke -> paint.toCanvasPaint(context)
            is MapPaint.StrokeWithBorder -> paint.toCanvasPaint(context)
        }

    private fun bakeBorderCanvasPaint(paint: MapPaint): PaintHolder<Paint>? =
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

    private val userPositionPaint = Paint()
        .apply {
            color = MapColor.Attribute(R.attr.colorPrimary).toColorInt(context)
            style = Paint.Style.FILL
        }
    private val terminalPaint = Paint()
        .apply {
            color = Color.RED
            style = Paint.Style.FILL
        }
    private val shortestPaint = Paint()
        .apply {
            strokeWidth = 4f
            color = Color.BLUE
            style = Paint.Style.STROKE
        }
    private val interestingPaint = Paint()
        .apply {
            color = Color.BLUE
            style = Paint.Style.FILL
        }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        mapData.onSizeChanged()
    }

    override fun onScaleBegin(x: Float, y: Float) {
        mapData.onScaleBegin()
    }

    override fun onScale(scale: Float) {
        mapData.onScale(scale)
    }

    override fun onRotateBegin() {
        mapData.onRotateBegin()
    }

    override fun onRotate(rotate: Float) {
        mapData.onRotate(rotate)
    }

    override fun onScroll(vX: Float, vY: Float) {
        mapData.onScroll(vX, vY)
    }

    override fun onClick(x: Float, y: Float) {
        mapData.onClick(x, y)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        mapData.draw(
            drawPath = { shape, paint, close ->
                canvas.drawPath(shape, paint, close)
            },
            drawCircle = { cx, cy, radius, paint ->
                canvas.drawCircle(cx, cy, radius, paint)
            },
            userPositionPaint,
            terminalPaint,
            shortestPaint,
            interestingPaint,
        )
    }

    fun centerAtUserPosition() {
        mapData.centerAtUserPosition()
    }

    fun centerAtPoint(point: PointD) {
        mapData.centerAtPoint(point)
    }
}
