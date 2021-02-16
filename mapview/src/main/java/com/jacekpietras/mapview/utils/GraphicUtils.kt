package com.jacekpietras.mapview.utils

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path

internal fun Canvas.drawPath(polygon: FloatArray, paint: Paint, close: Boolean = false) {
    val toDraw = Path()

    if (polygon.size >= 4) {
        toDraw.moveTo(polygon[0], polygon[1])

        for (i in 2 until polygon.size step 2)
            toDraw.lineTo(polygon[i], polygon[i + 1])

        if (close) toDraw.close()
    }

    drawPath(toDraw, paint)
}
