package com.jacekpietras.mapview.model

import com.jacekpietras.geometry.*

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

private inline fun <T> Iterable<T>.forEachWithNext(block: (a: T, b: T) -> Unit) {
    val iterator = iterator()
    if (!iterator.hasNext()) return
    var current = iterator.next()
    while (iterator.hasNext()) {
        val next = iterator.next()
        block(current, next)
        current = next
    }
}
