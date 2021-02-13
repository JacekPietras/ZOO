package com.jacekpietras.mapview.model

import android.graphics.PointF
import com.jacekpietras.mapview.model.DrawableOnCanvas

internal class PolygonF(val vertices: List<PointF>) : DrawableOnCanvas {

    fun contains(point: PointF): Boolean =
        com.jacekpietras.core.contains(vertices, point)
}
