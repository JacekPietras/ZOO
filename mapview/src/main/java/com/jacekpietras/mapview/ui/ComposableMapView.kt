package com.jacekpietras.mapview.ui

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.PaintingStyle
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import com.jacekpietras.core.PointD
import com.jacekpietras.core.RectD
import com.jacekpietras.mapview.model.MapItem
import com.jacekpietras.mapview.model.PaintHolder
import timber.log.Timber

@Composable
fun ComposableMapView(
    setOnPointPlacedListener: ((PointD) -> Unit)?,
    shortestPath: List<PointD>,
    clickOnWorld: PointD?,
    compass: Float,
    userPosition: PointD?,
    terminalPoints: List<PointD>,
    objectList: List<MapItem>,
    worldBounds: RectD,
) {
    if (objectList.isEmpty()) return

    val brush = Paint().apply {
        color = Color.Red
        isAntiAlias = true
        style = PaintingStyle.Stroke
        strokeWidth = 10f
    }

    Canvas(modifier = Modifier) {
        val mapData = MapViewLogic(
            getCurrentHeight = { size.height.toInt() },
            getCurrentWidth = { size.width.toInt() },
            doAnimation = { it(1f, 0f)},
            invalidate = { },
            bakeCanvasPaint = { PaintHolder.Static(brush) },
            bakeBorderCanvasPaint = { null },
        ).also {
            it.setOnPointPlacedListener = setOnPointPlacedListener
            it.worldBounds = worldBounds
            it.compass = compass
            it.userPosition = userPosition
            it.clickOnWorld = clickOnWorld
            it.shortestPath = shortestPath
            it.terminalPoints = terminalPoints
            it.objectList = objectList
        }
        Timber.e("dupa" + "redraw" + objectList.size + " world" + mapData.renderList?.size)

        mapData.renderList?.forEach {
            drawPath(it.shape, it.paint, it.close)
        }

//        mapData.draw(
//            drawPath = { a, b, c -> DrawPath(a, b, c) },
//            drawCircle = { _, _, _, _ -> },
//            brush,
//            brush,
//            brush,
//            brush,
//        )

        drawLine(
            start = Offset(
                x = 0f,
                y = 0f,
            ),
            end = Offset(
                x = size.width,
                y = size.height,
            ),
            color = Color(40, 193, 218),
            strokeWidth = Stroke.DefaultMiter
        )
    }


//
//    private val userPositionPaint = Paint()
//        .apply {
//            color = MapColor.Attribute(R.attr.colorPrimary).toColorInt(context)
//            style = Paint.Style.FILL
//        }
//    private val terminalPaint = Paint()
//        .apply {
//            color = Color.RED
//            style = Paint.Style.FILL
//        }
//    private val shortestPaint = Paint()
//        .apply {
//            strokeWidth = 4f
//            color = Color.BLUE
//            style = Paint.Style.STROKE
//        }
//    private val interestingPaint = Paint()
//        .apply {
//            color = Color.BLUE
//            style = Paint.Style.FILL
//        }
//
//    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
//        mapData.onSizeChanged()
//    }
//
//    override fun onScaleBegin(x: Float, y: Float) {
//        mapData.onScaleBegin()
//    }
//
//    override fun onScale(scale: Float) {
//        mapData.onScale(scale)
//    }
//
//    override fun onRotateBegin() {
//        mapData.onRotateBegin()
//    }
//
//    override fun onRotate(rotate: Float) {
//        mapData.onRotate(rotate)
//    }
//
//    override fun onScroll(vX: Float, vY: Float) {
//        mapData.onScroll(vX, vY)
//    }
//
//    override fun onClick(x: Float, y: Float) {
//        mapData.onClick(x, y)
//    }
//
//    override fun onDraw(canvas: Canvas) {
//        super.onDraw(canvas)
//
//        mapData.draw(
//            drawPath = canvas::drawPath,
//            drawCircle = canvas::drawCircle,
//            userPositionPaint,
//            terminalPaint,
//            shortestPaint,
//            interestingPaint,
//        )
//    }
//
//    fun centerAtUserPosition() {
//        mapData.centerAtUserPosition()
//    }
//
//    fun centerAtPoint(point: PointD) {
//        mapData.centerAtPoint(point)
//    }
}

fun DrawScope.drawPath(polygon: FloatArray, paint: Paint, close: Boolean = false) {
    val toDraw = Path()

    if (polygon.size >= 4) {
        toDraw.moveTo(polygon[0], polygon[1])

        for (i in 2 until polygon.size step 2)
            toDraw.lineTo(polygon[i], polygon[i + 1])

        if (close) toDraw.close()
    }

    drawPath(toDraw, paint.color)
}
