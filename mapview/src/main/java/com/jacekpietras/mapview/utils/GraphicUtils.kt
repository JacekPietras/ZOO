package com.jacekpietras.mapview.utils

import android.annotation.SuppressLint
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import com.jacekpietras.mapview.model.RenderItem

@SuppressLint("UseCompatLoadingForDrawables")
internal fun Canvas.drawMapObjects(list: List<RenderItem<Paint>>) {
    list.forEach {
        when (it) {
            is RenderItem.RenderPathItem -> drawPath(it.shape, it.paint, false)
            is RenderItem.RenderPolygonItem -> drawPath(it.shape, it.paint, true)
            is RenderItem.PointItem.RenderCircleItem -> drawCircle(it.cX, it.cY, it.radius, it.paint)
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
