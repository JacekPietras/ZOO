package com.jacekpietras.zoo.map.model

import com.jacekpietras.zoo.domain.model.PointD
import com.jacekpietras.zoo.domain.model.RectD
import com.jacekpietras.zoo.map.utils.contains
import com.jacekpietras.zoo.map.utils.containsLine

internal class PolygonD(val vertices: List<PointD>) : DrawableOnCanvas {

    fun intersects(rect: RectD): Boolean {
        if (contains(PointD(rect.left, rect.top))) return true
        vertices.zipWithNext().forEach {
            if (rect.containsLine(it.first, it.second)) return true
        }
        return false
    }

    fun contains(point: PointD): Boolean =
        contains(vertices, point)

    fun toFloat(): PolygonF =
        PolygonF(vertices.map { it.toFloat() })
}
