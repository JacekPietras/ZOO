package com.jacekpietras.zoo.map

import android.graphics.PointF
import android.graphics.RectF
import kotlin.math.abs

internal fun RectF.containsLine(p1: PointF, p2: PointF): Boolean {
    // Find min and max X for the segment
    var minX = p1.x
    var maxX = p2.x
    if (p1.x > p2.x) {
        minX = p2.x
        maxX = p1.x
    }

    // Find the intersection of the segment's and rectangle's x-projections
    if (maxX > right) maxX = right
    if (minX < left) minX = left

    // If their projections do not intersect return false
    if (minX > maxX) return false

    // Find corresponding min and max Y for min and max X we found before
    var minY = p1.y
    var maxY = p2.y
    val dx = p2.x - p1.x
    if (abs(dx) > 0.0000001) {
        val a = (p2.y - p1.y) / dx
        val b = p1.y - a * p1.x
        minY = a * minX + b
        maxY = a * maxX + b
    }
    if (minY > maxY) {
        val tmp = maxY
        maxY = minY
        minY = tmp
    }

    // Find the intersection of the segment's and rectangle's y-projections
    if (maxY > bottom) maxY = bottom
    if (minY < top) minY = top

    return minY <= maxY
}
