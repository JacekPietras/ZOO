package com.jacekpietras.zoo.map.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import com.jacekpietras.zoo.domain.model.PointD
import com.jacekpietras.zoo.domain.model.RectD
import com.jacekpietras.zoo.map.BuildConfig
import com.jacekpietras.zoo.map.model.DrawableOnCanvas
import com.jacekpietras.zoo.map.model.PathD
import com.jacekpietras.zoo.map.model.PolygonD
import com.jacekpietras.zoo.map.model.ViewCoordinates
import com.jacekpietras.zoo.map.utils.drawPath
import timber.log.Timber
import kotlin.math.min
import kotlin.math.sqrt

internal class MapView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : GesturedView(context, attrs, defStyleAttr) {

    var maxZoom: Double = 10.0
    var minZoom: Double = 2.0
    var worldRectangle: RectD = RectD()
        set(value) {
            field = value
            centerGpsCoordinate = PointD(value.centerX(), value.centerY())
            maxZoom = min(value.width(), value.height()) / 2
            minZoom = maxZoom / 6
            zoom = maxZoom / 3
        }
    private var _objectList: List<RenderItem> = emptyList()
    var objectList: List<MapItem> = emptyList()
        set(value) {
            Timber.v("Content changed")
            field = value
            _objectList = value.toRenderItems()
            cutOutNotVisible()
            invalidate()
        }

    var userPosition: PointD? = null
        set(value) {
            Timber.v("Position changed ${value?.x}")
            field = value
        }

    private lateinit var visibleGpsCoordinate: ViewCoordinates
    private var centerGpsCoordinate: PointD =
        PointD(worldRectangle.centerX(), worldRectangle.centerY())
    private var zoom: Double = 5.0
    private lateinit var renderList: List<RenderItem>
    private val debugTextPaint = Paint()
        .apply {
            color = Color.parseColor("#88000000")
            textSize = 30f
        }

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
        centerGpsCoordinate += PointD(
            vX / visibleGpsCoordinate.horizontalScale,
            vY / visibleGpsCoordinate.verticalScale
        )
        cutOutNotVisible()
        invalidate()
    }

    override fun onClick(x: Float, y: Float) {
        val point = PointD(x, y)
        renderList.forEach { item ->
            item.onClick?.let {
                when (item.shape) {
                    is PolygonD -> if (item.shape.contains(point)) {
                        it.invoke(x, y)
                    }
                    else -> throw IllegalStateException("Unknown shape type ${item.shape}")
                }
            }
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        renderList.forEach { item ->
            when (item.shape) {
                is PathD -> canvas.drawPath(item.shape, item.paint)
                is PolygonD -> canvas.drawPath(item.shape, item.paint)
                else -> throw IllegalStateException("Unknown shape type ${item.shape}")
            }
        }
        renderDebug(canvas)
    }

    private fun renderDebug(canvas: Canvas) {
        if (BuildConfig.DEBUG) {
            canvas.drawText("world:" + worldRectangle.toShortString(), 10f, 40f, debugTextPaint)
            canvas.drawText(
                "current:${visibleGpsCoordinate.visibleRect.toShortString()}",
                10f,
                80f,
                debugTextPaint
            )
            canvas.drawText("zoom:($minZoom) $zoom ($maxZoom)", 10f, 120f, debugTextPaint)
        }
    }

    private fun List<MapItem>.toRenderItems(): List<RenderItem> = map { item ->
        when (item.shape) {
            is PathD -> RenderItem(
                item.shape,
                item.paint.toCanvasPaint(context)
            )
            is PolygonD -> RenderItem(
                item.shape,
                item.paint.toCanvasPaint(context),
                item.onClick
            )
            else -> throw IllegalStateException("Unknown shape type ${item.shape}")
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
            centerGpsCoordinate = PointD(worldRectangle.centerX(), centerGpsCoordinate.y)
            result = true
        } else if (leftMargin < -0.00001) {
            centerGpsCoordinate -= PointD(leftMargin, 0.0)
            result = true
        } else if (rightMargin < -0.00001) {
            centerGpsCoordinate += PointD(rightMargin, 0.0)
            result = true
        }

        if (topMargin + bottomMargin < 0) {
            zoom = zoom.times(.99f).coerceAtMost(maxZoom).coerceAtLeast(minZoom)
            centerGpsCoordinate = PointD(centerGpsCoordinate.x, worldRectangle.centerY())
            result = true
        } else if (topMargin < -0.00001) {
            centerGpsCoordinate -= PointD(0.0, topMargin)
            result = true
        } else if (bottomMargin < -0.00001) {
            centerGpsCoordinate += PointD(0.0, bottomMargin)
            result = true
        }

        return result
    }

    private fun cutOutNotVisible() {
        if (width == 0 || height == 0) return

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

        val temp = mutableListOf<RenderItem>()

        _objectList.forEach { item ->
            when (item.shape) {
                is PolygonD -> {
                    visibleGpsCoordinate.transform(item.shape)?.let {
                        temp.add(
                            RenderItem(
                                PolygonD(it.vertices),
                                item.paint,
                                item.onClick
                            )
                        )
                    }
                }
                is PathD -> {
                    visibleGpsCoordinate.transform(item.shape).forEach {
                        temp.add(
                            RenderItem(
                                PathD(it.vertices),
                                item.paint
                            )
                        )
                    }
                }
            }
        }

        renderList = temp
        logVisibleShapes()
    }

    private fun logVisibleShapes() {
        if (BuildConfig.DEBUG) {
            val message = "Preparing render list: " + renderList.map { item ->
                when (item.shape) {
                    is PathD -> "PathsF " + item.shape.vertices.size
                    is PolygonD -> "PolygonF " + item.shape.vertices.size
                    else -> "Unknown"
                }
            } + " (${objectList.size})"
            Timber.v(message)
        }
    }

    private class RenderItem(
        val shape: DrawableOnCanvas,
        val paint: Paint,
        val onClick: ((x: Float, y: Float) -> Unit)? = null,
    )
}