package com.jacekpietras.zoo.map.model

import android.graphics.PointF
import android.graphics.RectF
import com.jacekpietras.zoo.map.utils.contains
import com.jacekpietras.zoo.map.utils.containsLine

internal class PolygonF(val vertices: List<PointF>) : DrawableOnCanvas {

    fun intersects(rect: RectF): Boolean {
        if (contains(PointF(rect.left, rect.top))) return true
        vertices.zipWithNext().forEach {
            if (rect.containsLine(it.first, it.second)) return true
        }
        return false
    }

    fun contains(point: PointF) :Boolean =
        contains(vertices, point)
}
