package com.jacekpietras.mapview.utils

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import com.jacekpietras.mapview.model.PathF
import com.jacekpietras.mapview.model.PolygonF

internal fun Canvas.drawPath(path: PathF, paint: Paint) {
    val toDraw = Path()
    path.vertices.forEachIndexed { i, point ->
        if (i == 0) {
            toDraw.moveTo(point.x, point.y)
        } else {
            toDraw.lineTo(point.x, point.y)
        }
    }
    drawPath(toDraw, paint)
}

internal fun Canvas.drawPath(polygon: PolygonF, paint: Paint) {
    val toDraw = Path()
    polygon.vertices.forEachIndexed { i, point ->
        if (i == 0) {
            toDraw.moveTo(point.x, point.y)
        } else {
            toDraw.lineTo(point.x, point.y)
        }
    }
    toDraw.close()
    drawPath(toDraw, paint)
}
