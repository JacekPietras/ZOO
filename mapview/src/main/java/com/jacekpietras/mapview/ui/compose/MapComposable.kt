package com.jacekpietras.mapview.ui.compose

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme.colors
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.jacekpietras.mapview.BuildConfig
import com.jacekpietras.mapview.model.ComposablePaint
import com.jacekpietras.mapview.model.RenderItem
import com.jacekpietras.mapview.model.RenderItem.PointItem.RenderBitmapItem
import com.jacekpietras.mapview.model.RenderItem.PointItem.RenderCircleItem
import com.jacekpietras.mapview.model.RenderItem.PointItem.RenderIconItem
import com.jacekpietras.mapview.model.RenderItem.RenderPathItem
import com.jacekpietras.mapview.model.RenderItem.RenderPolygonItem
import com.jacekpietras.mapview.ui.LastMapUpdate
import com.jacekpietras.mapview.ui.LastMapUpdate.medFps
import timber.log.Timber

@Composable
fun MapComposable(
    modifier: Modifier = Modifier,
    backgroundColor: Color,
    onSizeChanged: (Int, Int) -> Unit,
    onClick: ((Float, Float) -> Unit)? = null,
    onTransform: ((Float, Float, Float, Float, Float, Float) -> Unit)? = null,
    mapList: List<RenderItem<ComposablePaint>>,
) {
    val canvasItems = mapList.filterNot { it is RenderIconItem || it is RenderBitmapItem }

    Box {
        Canvas(
            modifier = Modifier
                .background(backgroundColor)
                .then(modifier)
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
                    is RenderBitmapItem -> Unit
                    is RenderIconItem -> Unit
                }
            }
        }
//        icons.forEach {
//            when (it) {
////                is RenderBitmapItem -> MapBitmap(it)
////                is RenderIconItem -> MapIcon(it)
//                else -> Unit
//            }
//        }

        if (BuildConfig.DEBUG) {
            LastMapUpdate.update()
            Text(
                text = "FPS: $medFps",
                modifier = Modifier.align(Alignment.BottomStart),
            )
        }
    }
}

@Composable
private fun MapIcon(item: RenderIconItem<ComposablePaint>) {
    with(LocalDensity.current) {
        Icon(
            modifier = Modifier
                .offset(x = item.cX.toDp() - item.iconSize.dp / 2, y = item.cY.toDp() - item.iconSize.dp / 2)
                .requiredSize(item.iconSize.dp),
            painter = painterResource(item.iconRes),
            contentDescription = null,
            tint = colors.onSurface,
        )
    }
}

@Composable
private fun MapBitmap(item: RenderBitmapItem<ComposablePaint>) {
    with(LocalDensity.current) {
        Image(
            modifier = Modifier
                .offset(x = (item.cX - item.bitmap.width / 2).toDp(), y = (item.cY - item.bitmap.height).toDp()),
            bitmap = item.bitmap.asImageBitmap(),
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
