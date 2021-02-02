package com.jacekpietras.zoo.map.utils

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

internal fun contains(list: List<PointF>, point: PointF): Boolean {
    // ray casting algorithm http://rosettacode.org/wiki/Ray-casting_algorithm
    var crossings = 0

    // for each edge
    for (i in list.indices) {
        val a: PointF = list[i]
        var j = i + 1
        //to close the last edge, you have to take the first point of your polygon
        if (j >= list.size) {
            j = 0
        }
        val b: PointF = list[j]
        if (rayCrossesSegment(point, a, b)) {
            crossings++
        }
    }

    // odd number of crossings?
    return crossings % 2 == 1
}

private fun rayCrossesSegment(point: PointF, a: PointF, b: PointF): Boolean {
    // Ray Casting algorithm checks, for each segment, if the point is
    // 1) to the left of the segment and
    // 2) not above nor below the segment. If these two conditions are met, it returns true
    var px: Float = point.x
    var py: Float = point.y
    var ax: Float = a.x
    var ay: Float = a.y
    var bx: Float = b.x
    var by: Float = b.y
    if (ay > by) {
        ax = b.x
        ay = b.y
        bx = a.x
        by = a.y
    }
    // alter longitude to cater for 180 degree crossings
    if (px < 0 || ax < 0 || bx < 0) {
        px += 360f
        ax += 360f
        bx += 360f
    }
    // if the point has the same latitude as a or b, increase slightly py
    if (py == ay || py == by) py += 0.00000001f


    // if the point is above, below or to the right of the segment, it returns false
    return if (py > by || py < ay || px > ax.coerceAtLeast(bx)) {
        false
    } else if (px < ax.coerceAtMost(bx)) {
        true
    } else {
        val red = if (ax != bx) (by - ay) / (bx - ax) else java.lang.Float.POSITIVE_INFINITY
        val blue = if (ax != px) (py - ay) / (px - ax) else java.lang.Float.POSITIVE_INFINITY
        blue >= red
    }
}
