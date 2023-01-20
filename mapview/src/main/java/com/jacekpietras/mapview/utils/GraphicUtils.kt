package com.jacekpietras.mapview.utils

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import androidx.compose.foundation.layout.offset
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.platform.LocalDensity
import com.jacekpietras.mapview.model.Pivot
import com.jacekpietras.mapview.model.RenderItem


@SuppressLint("UseCompatLoadingForDrawables")
internal fun Canvas.drawMapObjects(context: Context, list: List<RenderItem<Paint>>) {
    list.forEach {
        when (it) {
            is RenderItem.RenderPathItem -> drawPath(it.shape, it.paint, false)
            is RenderItem.RenderPolygonItem -> drawPath(it.shape, it.paint, true)
            is RenderItem.PointItem.RenderCircleItem -> drawCircle(it.cX, it.cY, it.radius, it.paint)
            is RenderItem.PointItem.RenderIconItem -> {
                // todo positioning is incorrect, also does not use pivot
                context.resources.getDrawable(it.iconRes, null).draw(this)
            }
            is RenderItem.PointItem.RenderBitmapItem -> drawBitmap(it.bitmap, it.cXpivoted, it.cYpivoted, null)
        }
    }
}

private fun Canvas.drawPath(polygon: FloatArray, paint: Paint, close: Boolean = false) {
    val toDraw = Path()

    if (polygon.size >= 4) {
        toDraw.moveTo(polygon[0], polygon[1])

        for (i in 2 until polygon.size step 2)
            toDraw.lineTo(polygon[i], polygon[i + 1])

        if (close) toDraw.close()
    }

    drawPath(toDraw, paint)
}
