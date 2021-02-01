package com.jacekpietras.zoo.map.model

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PointF

internal class PathF(val list: List<PointF>) {

    constructor(vararg points: Pair<Float, Float>)
            : this(points.map { PointF(it.first, it.second) })
}

internal class PathsF(val list: List<List<PointF>>)

internal fun Canvas.drawPath(paths: PathsF, paint: Paint) {
    paths.list.forEach { path ->
        val toDraw = Path()
        path.forEachIndexed { i, point ->
            if (i == 0) {
                toDraw.moveTo(point.x, point.y)
            } else {
                toDraw.lineTo(point.x, point.y)
            }
        }
        drawPath(toDraw, paint)
    }
}
