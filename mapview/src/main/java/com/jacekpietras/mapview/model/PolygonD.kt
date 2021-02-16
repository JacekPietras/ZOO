package com.jacekpietras.mapview.model

import com.jacekpietras.core.PointD
import com.jacekpietras.core.RectD
import com.jacekpietras.core.containsLine
import com.jacekpietras.core.polygonContains

class PolygonD(val vertices: List<PointD>) {

    fun intersects(rect: RectD): Boolean {
        if (contains(PointD(rect.left, rect.top))) return true
        vertices.zipWithNext().forEach {
            if (rect.containsLine(it.first, it.second)) return true
        }
        return false
    }

    fun contains(point: PointD): Boolean =
        polygonContains(vertices, point)
}
