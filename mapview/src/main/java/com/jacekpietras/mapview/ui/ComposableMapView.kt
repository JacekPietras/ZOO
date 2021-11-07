package com.jacekpietras.mapview.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.input.pointer.consumeAllChanges
import androidx.compose.ui.input.pointer.pointerInput
import com.jacekpietras.mapview.model.ComposablePaint

@Composable
fun ComposableMapView(
//    setOnPointPlacedListener: ((PointD) -> Unit)?,
//    shortestPath: List<PointD>,
//    clickOnWorld: PointD?,
//    compass: Float,
//    userPosition: PointD?,
//    terminalPoints: List<PointD>,
//    objectList: List<MapItem>,
//    worldBounds: RectD,
    mapData: MapViewLogic<ComposablePaint>,
) {
    if (mapData.renderList.isNullOrEmpty()) return

    Canvas(modifier = Modifier
        .pointerInput(Unit) {
            detectDragGestures { change, dragAmount ->
                change.consumeAllChanges()
                mapData.onScroll(dragAmount.x, dragAmount.y)
            }
        }
    ) {
        mapData.currentHeight = size.height.toInt()
        mapData.currentWidth = size.width.toInt()

        mapData.draw(
            drawPath = this::drawPath,
            drawCircle = { cx, cy, radius, paint ->
                drawCircle(
                    color = paint.color,
                    radius = radius,
                    center = Offset(cx, cy),
                )
            },
            ComposablePaint.Fill(color = Color.Green),
            ComposablePaint.Fill(color = Color.Red),
            ComposablePaint.Stroke(color = Color.Blue),
            ComposablePaint.Fill(color = Color.Blue),
        )
    }

// TODO implement those:
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
//    fun centerAtUserPosition() {
//        mapData.centerAtUserPosition()
//    }
//
//    fun centerAtPoint(point: PointD) {
//        mapData.centerAtPoint(point)
//    }
}

private fun DrawScope.drawPath(polygon: FloatArray, paint: ComposablePaint, close: Boolean = false) {
    val toDraw = Path()

    if (polygon.size >= 4) {
        toDraw.moveTo(polygon[0], polygon[1])

        for (i in 2 until polygon.size step 2)
            toDraw.lineTo(polygon[i], polygon[i + 1])

        if (close) toDraw.close()
    }
    drawPath(
        path = toDraw,
        color = paint.color,
        alpha = paint.alpha,
        style = when (paint) {
            is ComposablePaint.Stroke -> paint.stroke
            is ComposablePaint.Fill -> Fill
        }
    )
}
