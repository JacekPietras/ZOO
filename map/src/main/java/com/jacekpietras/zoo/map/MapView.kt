package com.jacekpietras.zoo.map

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import androidx.core.graphics.contains
import androidx.core.graphics.minus
import androidx.core.graphics.plus
import androidx.core.graphics.toRectF
import kotlin.math.sqrt

internal class MapView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : GesturedView(context, attrs, defStyleAttr) {

    var maxZoom: Float = 10f
    var minZoom: Float = 2f
    var worldRectangle: RectF = RectF(10f, 10f, 30f, 30f)
        set(value) {
            field = value
            centerGpsCoordinate = PointF(worldRectangle.centerX(), worldRectangle.centerY())
        }
    var objectList: List<MapItem> = emptyList()
        set(value) {
            field = value
            cutOutNotVisible()
            invalidate()
        }

    private lateinit var visibleGpsCoordinate: ViewCoordinates
    private var centerGpsCoordinate: PointF =
        PointF(worldRectangle.centerX(), worldRectangle.centerY())
    private var zoom: Float = 5f
    private lateinit var renderList: List<MapItem>

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        cutOutNotVisible()
    }

    override fun onScale(scale: Float) {
        zoom = zoom.div(sqrt(scale)).coerceAtMost(maxZoom).coerceAtLeast(minZoom)
        cutOutNotVisible()
        invalidate()
    }

    override fun onScroll(vX: Float, vY: Float) {
        centerGpsCoordinate += PointF(
            vX / visibleGpsCoordinate.horizontalScale,
            vY / visibleGpsCoordinate.verticalScale
        )
        cutOutNotVisible()
        invalidate()
    }

    override fun onClick(x: Float, y: Float) {
        val point = PointF(x, y)
        renderList.forEach { item ->
            when (item.shape) {
                is Rect -> {
                    if (item.onClick != null && item.shape.toRectF().contains(point)) {
                        item.onClick.invoke(x, y)
                    }
                }
                is PolygonF -> {
                    if (item.onClick != null && item.shape.contains(point)) {
                        item.onClick.invoke(x, y)
                    }
                }
            }
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        renderList.forEach { item ->
            when (item.shape) {
                is Rect -> canvas.drawRect(item.shape, item.paint)
                is PathsF -> canvas.drawPath(item.shape, item.paint)
                is PolygonF -> canvas.drawPath(item.shape, item.paint)
                else -> throw IllegalStateException("Unknown shape type ${item.shape}")
            }
        }
    }

    private fun preventedGoingOutsideWorld(): Boolean {
        val leftMargin = visibleGpsCoordinate.visibleRect.left - worldRectangle.left
        val rightMargin = worldRectangle.right - visibleGpsCoordinate.visibleRect.right
        val topMargin = visibleGpsCoordinate.visibleRect.top - worldRectangle.top
        val bottomMargin = worldRectangle.bottom - visibleGpsCoordinate.visibleRect.bottom
        var result = false

        if (leftMargin + rightMargin < 0) {
            zoom = zoom.times(.99f).coerceAtMost(maxZoom).coerceAtLeast(minZoom)
            centerGpsCoordinate = PointF(worldRectangle.centerX(), centerGpsCoordinate.y)
            result = true
        } else if (leftMargin < -0.00001) {
            centerGpsCoordinate -= PointF(leftMargin, 0f)
            result = true
        } else if (rightMargin < -0.00001) {
            centerGpsCoordinate += PointF(rightMargin, 0f)
            result = true
        }

        if (topMargin + bottomMargin < 0) {
            zoom = zoom.times(.99f).coerceAtMost(maxZoom).coerceAtLeast(minZoom)
            centerGpsCoordinate = PointF(centerGpsCoordinate.x, worldRectangle.centerY())
            result = true
        } else if (topMargin < -0.00001) {
            centerGpsCoordinate -= PointF(0f, topMargin)
            result = true
        } else if (bottomMargin < -0.00001) {
            centerGpsCoordinate += PointF(0f, bottomMargin)
            result = true
        }

        return result
    }

    private fun transform(item: MapItem): MapItem =
        @Suppress("IMPLICIT_CAST_TO_ANY")
        when (item.shape) {
            is PointF -> MapItem(
                visibleGpsCoordinate.transform(item.shape),
                item.paint,
            )
            is RectF -> MapItem(
                visibleGpsCoordinate.transform(item.shape),
                item.paint,
                item.onClick,
            )
            is PolygonF -> MapItem(
                visibleGpsCoordinate.transform(item.shape),
                item.paint,
                item.onClick,
            )
            is PathF -> MapItem(
                visibleGpsCoordinate.transform(item.shape),
                item.paint,
            )
            else -> throw IllegalStateException("Unknown shape type ${item.shape}")
        }


    private fun cutOutNotVisible() {
        visibleGpsCoordinate = ViewCoordinates.create(
            centerGpsCoordinate,
            zoom,
            width,
            height,
        )

        if (preventedGoingOutsideWorld()) {
            cutOutNotVisible()
            return
        }

        renderList = objectList
            .asSequence()
            .filter { item ->
                when (item.shape) {
                    is PointF -> visibleGpsCoordinate.contains(item.shape)
                    is RectF -> visibleGpsCoordinate.intersects(item.shape)
                    is PolygonF -> visibleGpsCoordinate.intersects(item.shape)
                    is PathF -> true
                    else -> throw IllegalStateException("Unknown shape type ${item.shape}")
                }
            }
            .map(::transform)
            .filter { item ->
                when (item.shape) {
                    is PathsF -> item.shape.list.isNotEmpty()
                    else -> true
                }
            }
            .toList()

        logVisibleShapes()
    }

    private fun logVisibleShapes() {
        if (BuildConfig.DEBUG) {
            Log.i(
                "dupa",
                System.currentTimeMillis().toString() + "     " + renderList.map { item ->
                    when (item.shape) {
                        is RectF -> "RectF"
                        is PointF -> "PointF"
                        is Rect -> "Rect"
                        is Point -> "Point"
                        is PathsF -> "PathsF " + item.shape.list.map { it.size }.toString()
                        is PolygonF -> "PolygonF " + item.shape.list.size.toString()
                        else -> "Unknown"
                    }
                }.toString()
            )
        }
    }
}