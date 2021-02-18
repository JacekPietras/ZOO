package com.jacekpietras.mapview.model

import com.jacekpietras.core.*

class PolygonD(val vertices: List<PointD>) {

    fun intersects(rect: RectD): Boolean {
        if (contains(PointD(rect.left, rect.top))) return true
        vertices.forEachWithNext { first, second ->
            if (rect.containsLine(first, second)) return true
        }
        return false
    }

    fun contains(point: PointD): Boolean =
        polygonContains(vertices, point)
}
