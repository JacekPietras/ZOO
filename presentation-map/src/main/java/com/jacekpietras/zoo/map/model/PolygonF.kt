package com.jacekpietras.zoo.map.model

import android.graphics.PointF

internal class PolygonF(val vertices: List<PointF>) : DrawableOnCanvas{

    fun contains(point: PointF): Boolean =
        com.jacekpietras.core.contains(vertices, point)
}
