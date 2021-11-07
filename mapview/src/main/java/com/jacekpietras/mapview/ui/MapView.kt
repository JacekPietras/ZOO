package com.jacekpietras.mapview.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import com.jacekpietras.core.PointD
import com.jacekpietras.core.RectD
import com.jacekpietras.mapview.R
import com.jacekpietras.mapview.model.MapColor
import com.jacekpietras.mapview.model.MapItem
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
    var objectList: List<MapItem>
        get() = mapData.objectList
        set(value) {
            mapData.objectList = value
        }
    var worldBounds: RectD
        get() = mapData.worldBounds
        set(value) {
            mapData.worldBounds = value
        }
    private val paintBaker = ViewPaintBaker()

    private val mapData = MapViewLogic(
        getCurrentHeight = { height },
        getCurrentWidth = { width },
        doAnimation = { lambda -> doAnimation(true, lambda) },
        invalidate = { invalidate() },
        bakeCanvasPaint = { paintBaker.bakeCanvasPaint(context, it) },
        bakeBorderCanvasPaint = { paintBaker.bakeBorderCanvasPaint(context, it) },
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
            drawPath = canvas::drawPath,
            drawCircle = canvas::drawCircle,
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
