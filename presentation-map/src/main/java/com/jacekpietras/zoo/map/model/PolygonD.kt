package com.jacekpietras.zoo.map.model

import com.jacekpietras.core.PointD
import com.jacekpietras.core.RectD
import com.jacekpietras.core.containsLine

internal class PolygonD(val vertices: List<PointD>) : DrawableOnCanvas {

    fun intersects(rect: RectD): Boolean {
        if (contains(PointD(rect.left, rect.top))) return true
        vertices.zipWithNext().forEach {
            if (rect.containsLine(it.first, it.second)) return true
        }
        return false
    }

    fun contains(point: PointD): Boolean =
        com.jacekpietras.core.contains(vertices, point)

    fun toFloat(): PolygonF =
        PolygonF(vertices.map { it.toFloat() })
}
