package com.jacekpietras.mapview.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Modifier
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
    onScroll: (Float, Float) -> Unit,
    onSizeChanged: (Int, Int) -> Unit,
    onClick: (Float, Float) -> Unit,
    onRotate: (Float) -> Unit,
    onScale: (Float) -> Unit,
    state: State<String?>,
) {
    Timber.e("dupa Recomposing screen - ${state.value}")

    Canvas(modifier = Modifier
        .pointerInput(Unit) {
            detectTransformGestures { _, pan, zoomChange, rotationChange ->
                onScroll(-pan.x, -pan.y)
                onScale(1/zoomChange)
                onRotate(-rotationChange)
            }
        }
        .pointerInput(Unit) {
            detectTapGestures(
                onTap = { offset ->
                    onClick(offset.x, offset.y)
                },
            )
        }
    ) {
        onSizeChanged(size.width.toInt(), size.height.toInt())

        mapData.draw(
            drawPath = this::drawPath,
            drawCircle = { cx, cy, radius, paint ->
                drawCircle(
                    color = paint.color,
                    radius = radius,
                    center = Offset(cx, cy),
                )
            },
        )
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
