package com.jacekpietras.mapview.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import com.jacekpietras.mapview.model.ComposablePaint
import com.jacekpietras.mapview.ui.MapViewLogic.RenderItem.RenderCircleItem
import com.jacekpietras.mapview.ui.MapViewLogic.RenderItem.RenderIconItem
import com.jacekpietras.mapview.ui.MapViewLogic.RenderItem.RenderPathItem
import com.jacekpietras.mapview.ui.MapViewLogic.RenderItem.RenderPolygonItem
import timber.log.Timber

@Composable
fun ComposableMapView(
    modifier: Modifier = Modifier,
    onSizeChanged: (Int, Int) -> Unit,
    onClick: ((Float, Float) -> Unit)? = null,
    onTransform: ((Float, Float, Float, Float, Float, Float) -> Unit)? = null,
    mapList: List<MapViewLogic.RenderItem<ComposablePaint>>,
) {
    val (icons, canvasItems) = mapList.partition { it is RenderIconItem }

    Box {
        Canvas(
            modifier = modifier
                .clipToBounds()
                .addOnTransform(onTransform)
                .addOnClick(onClick)
        ) {
            val (width, height) = size.width.toInt() to size.height.toInt()
            if (width > 0 && height > 0) {
                onSizeChanged(width, height)
            }

            canvasItems.forEach {
                when (it) {
                    is RenderPathItem -> drawPath(it.shape, it.paint, false)
                    is RenderPolygonItem -> drawPath(it.shape, it.paint, true)
                    is RenderCircleItem -> drawCircleSafe(it.paint.color, it.radius, Offset(it.cX, it.cY))
                    is RenderIconItem -> Unit
                }
            }
        }
        icons.forEach {
            MapIcon(it as RenderIconItem<ComposablePaint>)
        }
    }
}

@Composable
private fun MapIcon(item: RenderIconItem<ComposablePaint>) {
    with(LocalDensity.current) {
        Image(
            modifier = Modifier.offset(x = item.cX.toDp(), y = item.cY.toDp()),
            painter = painterResource(item.iconRes),
            contentDescription = null,
        )
    }
}

private fun DrawScope.drawCircleSafe(
    color: Color,
    radius: Float,
    center: Offset,
) {
    if (center == Offset(Float.NaN, Float.NaN)) {
        Timber.w("Could Crash at drawCircle, offset:$center")
    } else {
        drawCircle(
            color,
            radius,
            center,
        )
    }
}

private fun Modifier.addOnTransform(onTransform: ((Float, Float, Float, Float, Float, Float) -> Unit)?): Modifier {
    onTransform ?: return this

    return pointerInput(Unit) {
        detectTransformGestures { c, pan, zoomChange, rotationChange ->
            onTransform(
                c.x,
                c.y,
                1 / zoomChange,
                -rotationChange,
                -pan.x,
                -pan.y,
            )
        }
    }
}

private fun Modifier.addOnClick(onClick: ((Float, Float) -> Unit)?): Modifier {
    onClick ?: return this

    return pointerInput(Unit) {
        detectTapGestures(
            onTap = { offset ->
                onClick(offset.x, offset.y)
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
            is ComposablePaint.Circle,
            is ComposablePaint.Fill -> Fill
        }
    )
}
