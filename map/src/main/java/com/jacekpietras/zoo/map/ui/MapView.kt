package com.jacekpietras.zoo.map.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PointF
import android.graphics.RectF
import android.util.AttributeSet
import android.util.Log
import androidx.core.graphics.minus
import androidx.core.graphics.plus
import com.jacekpietras.zoo.map.BuildConfig
import com.jacekpietras.zoo.map.model.DrawableOnCanvas
import com.jacekpietras.zoo.map.model.PathF
import com.jacekpietras.zoo.map.model.PolygonF
import com.jacekpietras.zoo.map.model.ViewCoordinates
import com.jacekpietras.zoo.map.utils.drawPath
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
    private var _objectList: List<RenderItem> = emptyList()
    var objectList: List<MapItem> = emptyList()
        set(value) {
            field = value
            _objectList =
                value.map { item ->
                    when (item.shape) {
                        is PathF -> RenderItem(
                            item.shape,
                            item.paint.toCanvasPaint(context)
                        )
                        is PolygonF -> RenderItem(
                            item.shape,
                            item.paint.toCanvasPaint(context),
                            item.onClick
                        )
                        else -> throw IllegalStateException("Unknown shape type ${item.shape}")
                    }
                }
            cutOutNotVisible()
            invalidate()
        }

    private lateinit var visibleGpsCoordinate: ViewCoordinates
    private var centerGpsCoordinate: PointF =
        PointF(worldRectangle.centerX(), worldRectangle.centerY())
    private var zoom: Float = 5f
    private lateinit var renderList: List<RenderItem>

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
            item.onClick?.let {
                when (item.shape) {
                    is PolygonF -> if (item.shape.contains(point)) {
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
                is PathF -> canvas.drawPath(item.shape, item.paint)
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

        val temp = mutableListOf<RenderItem>()

        _objectList.forEach { item ->
            when (item.shape) {
                is PolygonF -> {
                    visibleGpsCoordinate.transform(item.shape)?.let {
                        temp.add(
                            RenderItem(
                                PolygonF(it.vertices),
                                item.paint,
                                item.onClick
                            )
                        )
                    }
                }
                is PathF -> {
                    visibleGpsCoordinate.transform(item.shape).forEach {
                        temp.add(
                            RenderItem(
                                PathF(it.vertices),
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
            Log.i(
                "dupa",
                System.currentTimeMillis().toString() + "     " + renderList.map { item ->
                    when (item.shape) {
                        is PathF -> "PathsF " + item.shape.vertices.size.toString()
                        is PolygonF -> "PolygonF " + item.shape.vertices.size.toString()
                        else -> "Unknown"
                    }
                }.toString()
            )
        }
    }

    private class RenderItem(
        val shape: DrawableOnCanvas,
        val paint: Paint,
        val onClick: ((x: Float, y: Float) -> Unit)? = null,
    )
}