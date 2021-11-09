package com.jacekpietras.mapview.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.input.pointer.pointerInput
import com.jacekpietras.mapview.model.ComposablePaint
import timber.log.Timber

@Composable
fun ComposableMapView(
    mapData: MapViewLogic<ComposablePaint>,
    state: State<String?>,
) {
    Timber.e("dupa Recomposing screen - ${state.value}")

    Canvas(modifier = Modifier
        .fillMaxSize()
        .clipToBounds()
        .pointerInput(Unit) {
            detectTransformGestures { _, pan, zoomChange, rotationChange ->
                mapData.onTransform(
                    scale = 1 / zoomChange,
                    rotate = -rotationChange,
                    vX = -pan.x,
                    vY = -pan.y,
                )
            }
        }
        .pointerInput(Unit) {
            detectTapGestures(
                onTap = { offset ->
                    mapData.onClick(offset.x, offset.y)
                },
            )
        }
    ) {
        mapData.onSizeChanged(size.width.toInt(), size.height.toInt())

        mapData.getFullRenderList().forEach {
            when (it) {
                is MapViewLogic.RenderPathItem -> drawPath(it.shape, it.paint, it.close)
                is MapViewLogic.RenderCircleItem -> drawCircle(it.paint.color, it.radius, Offset(it.cX, it.cY))
            }
        }
    }
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
