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
import androidx.compose.ui.composed
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
import com.jacekpietras.mapview.model.Pivot
import com.jacekpietras.mapview.model.RenderItem
import com.jacekpietras.mapview.model.RenderItem.PointItem.RenderBitmapItem
import com.jacekpietras.mapview.model.RenderItem.PointItem.RenderCircleItem
import com.jacekpietras.mapview.model.RenderItem.PointItem.RenderIconItem
import com.jacekpietras.mapview.model.RenderItem.RenderPathItem
import com.jacekpietras.mapview.model.RenderItem.RenderPolygonItem
import com.jacekpietras.mapview.ui.LastMapUpdate
import com.jacekpietras.mapview.ui.LastMapUpdate.cutoE
import com.jacekpietras.mapview.ui.LastMapUpdate.cutoS
import com.jacekpietras.mapview.ui.LastMapUpdate.trans
import com.jacekpietras.mapview.ui.LastMapUpdate.medFps
import com.jacekpietras.mapview.ui.LastMapUpdate.mergE
import com.jacekpietras.mapview.ui.LastMapUpdate.moveE
import com.jacekpietras.mapview.ui.LastMapUpdate.rendE
import com.jacekpietras.mapview.ui.LastMapUpdate.rendS
import com.jacekpietras.mapview.ui.LastMapUpdate.sortE
import com.jacekpietras.mapview.ui.LastMapUpdate.sortS
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
    rendS = System.nanoTime()

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

            mapList.forEach {
                when (it) {
                    is RenderPathItem -> drawPath(it.shape, it.paint, false)
                    is RenderPolygonItem -> drawPath(it.shape, it.paint, true)
                    is RenderCircleItem -> drawCircleSafe(it.paint.color, it.radius, Offset(it.cX, it.cY))
                    else -> Unit
                }
            }
        }
        mapList.forEach {
            when (it) {
                is RenderBitmapItem -> MapBitmap(it)
                is RenderIconItem -> MapIcon(it)
                else -> Unit
            }
        }

        if (BuildConfig.DEBUG) {
            LastMapUpdate.update()
            Text(
                text = "FPS: $medFps",
                modifier = Modifier.align(Alignment.BottomStart),
            )
        }

        val prevRendE = rendE
        rendE = System.nanoTime()
        if (trans > 0) {

            Timber.d(
                "Perf: Render: Full: ${trans toMs rendE}, from prev ${prevRendE toMs rendE}\n" +
                        "    [pass to vm] ${trans toMs cutoS}\n" +
                        "    [coord prep] ${cutoS toMs moveE}\n" +
                        "    [ translate] ${moveE toMs sortS}\n" +
                        "    [      sort] ${sortS toMs sortE}\n" +
                        "    [       sum] ${sortE toMs mergE}\n" +
                        "    [invali req] ${mergE toMs cutoE}\n" +
                        "    [invalidate] ${cutoE toMs rendS}\n" +
                        "    [    render] ${rendS toMs rendE}"
            )
        }
        lastUpdate = System.currentTimeMillis()
    }
    Timber.d("Perf: draw ${System.currentTimeMillis() - before} ms")
}

private infix fun Long.toMs(right:Long)=
    "${(right - this) / 10_000 / 1_00.0} ms"

@Composable
private fun MapIcon(item: RenderIconItem<ComposablePaint>) {
    Icon(
        modifier = Modifier
            .offset(item)
            .requiredSize(item.height.dp),
        painter = painterResource(item.iconRes),
        contentDescription = null,
        tint = colors.onSurface,
    )
}

@Composable
private fun MapBitmap(item: RenderBitmapItem<ComposablePaint>) {
    Image(
        modifier = Modifier
            .offset(item),
        bitmap = item.bitmap.asImageBitmap(),
        contentDescription = null,
    )
}

private fun <T> Modifier.offset(item: RenderIconItem<T>): Modifier =
    composed {
        with(LocalDensity.current) {
            with(item) {
                when (pivot) {
                    Pivot.TOP -> offset(
                        x = cX.toDp() - width.dp / 2,
                        y = cY.toDp(),
                    )
                    Pivot.BOTTOM -> offset(
                        x = cX.toDp() - width.dp / 2,
                        y = cY.toDp() - height.dp,
                    )
                    Pivot.LEFT -> offset(
                        x = cX.toDp(),
                        y = cY.toDp() - height.dp / 2,
                    )
                    Pivot.RIGHT -> offset(
                        x = cX.toDp() - width.dp,
                        y = cY.toDp() - height.dp / 2,
                    )
                    Pivot.CENTER -> offset(
                        x = cX.toDp() - width.dp / 2,
                        y = cY.toDp() - height.dp / 2,
                    )
                }
            }
        }
    }

private fun <T> Modifier.offset(item: RenderBitmapItem<T>): Modifier =
    composed {
        with(LocalDensity.current) {
            with(item) {
                when (pivot) {
                    Pivot.TOP -> offset(
                        x = (cX - width / 2).toDp(),
                        y = cY.toDp(),
                    )
                    Pivot.BOTTOM -> offset(
                        x = (cX - width / 2).toDp(),
                        y = (cY - height).toDp(),
                    )
                    Pivot.LEFT -> offset(
                        x = cX.toDp(),
                        y = (cY - height / 2).toDp(),
                    )
                    Pivot.RIGHT -> offset(
                        x = (cX - width).toDp(),
                        y = (cY - height / 2).toDp(),
                    )
                    Pivot.CENTER -> offset(
                        x = (cX - width / 2).toDp(),
                        y = (cY - height / 2).toDp(),
                    )
                }
            }
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
