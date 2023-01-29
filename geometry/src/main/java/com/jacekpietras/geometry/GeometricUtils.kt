package com.jacekpietras.geometry

import android.graphics.PointF
import android.graphics.RectF
import kotlin.math.*

fun RectD.containsLine(p1: PointD, p2: PointD): Boolean {
    // Find min and max X for the segment
    var minX = p1.x
    var maxX = p2.x
    if (p1.x > p2.x) {
        minX = p2.x
        maxX = p1.x
    }

    // Find the intersection of the segment's and rectangle's x-projections
    if (right > left) {
        if (maxX > right) maxX = right
        if (minX < left) minX = left
    } else {
        if (maxX > left) maxX = left
        if (minX < right) minX = right
    }

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
    if (bottom > top) {
        if (maxY > bottom) maxY = bottom
        if (minY < top) minY = top
    } else {
        if (maxY > top) maxY = top
        if (minY < bottom) minY = bottom
    }

    return minY <= maxY
}

fun RectF.containsLine(p1x: Float, p1y: Float, p2x: Float, p2y: Float): Boolean {
    // Find min and max X for the segment
    var minX = p1x
    var maxX = p2x
    if (p1x > p2x) {
        minX = p2x
        maxX = p1x
    }

    // Find the intersection of the segment's and rectangle's x-projections
    if (right > left) {
        if (maxX > right) maxX = right
        if (minX < left) minX = left
    } else {
        if (maxX > left) maxX = left
        if (minX < right) minX = right
    }

    // If their projections do not intersect return false
    if (minX > maxX) return false

    // Find corresponding min and max Y for min and max X we found before
    var minY = p1y
    var maxY = p2y
    val dx = p2x - p1x
    if (abs(dx) > 0.0000001) {
        val a = (p2y - p1y) / dx
        val b = p1y - a * p1x
        minY = a * minX + b
        maxY = a * maxX + b
    }
    if (minY > maxY) {
        val tmp = maxY
        maxY = minY
        minY = tmp
    }

    // Find the intersection of the segment's and rectangle's y-projections
    if (bottom > top) {
        if (maxY > bottom) maxY = bottom
        if (minY < top) minY = top
    } else {
        if (maxY > top) maxY = top
        if (minY < bottom) minY = bottom
    }

    return minY <= maxY
}

fun RectD.containsLine(
    p1x: Double,
    p1y: Double,
    p2x: Double,
    p2y: Double
): Boolean {
    // Find min and max X for the segment
    var minX = p1x
    var maxX = p2x
    if (p1x > p2x) {
        minX = p2x
        maxX = p1x
    }

    // Find the intersection of the segment's and rectangle's x-projections
    if (right > left) {
        if (maxX > right) maxX = right
        if (minX < left) minX = left
    } else {
        if (maxX > left) maxX = left
        if (minX < right) minX = right
    }

    // If their projections do not intersect return false
    if (minX > maxX) return false

    // Find corresponding min and max Y for min and max X we found before
    var minY = p1y
    var maxY = p2y
    val dx = p2x - p1x
    if (abs(dx) > 0.0000001) {
        val a = (p2y - p1y) / dx
        val b = p1y - a * p1x
        minY = a * minX + b
        maxY = a * maxX + b
    }
    if (minY > maxY) {
        val tmp = maxY
        maxY = minY
        minY = tmp
    }

    // Find the intersection of the segment's and rectangle's y-projections
    if (bottom > top) {
        if (maxY > bottom) return minY <= bottom
        if (minY < top) return top <= maxY
    } else {
        if (maxY > top) return minY <= top
        if (minY < bottom) return bottom <= maxY
    }

    return minY <= maxY
}

fun polygonContains(list: List<PointD>, point: PointD): Boolean {
    // ray casting algorithm http://rosettacode.org/wiki/Ray-casting_algorithm
    var crossings = 0

    // for each edge
    for (i in list.indices) {
        val a: PointD = list[i]
        var j = i + 1
        //to close the last edge, you have to take the first point of your polygon
        if (j >= list.size) {
            j = 0
        }
        val b: PointD = list[j]
        if (rayCrossesSegment(point, a, b)) {
            crossings++
        }
    }

    // odd number of crossings?
    return crossings % 2 == 1
}

fun polygonContains(list: FloatArray, px: Float, py: Float): Boolean {
    // ray casting algorithm http://rosettacode.org/wiki/Ray-casting_algorithm
    var crossings = 0

    // for each edge
    for (i in list.indices step 2) {
        val ax = list[i]
        val ay = list[i + 1]
        var j = i + 2
        //to close the last edge, you have to take the first point of your polygon
        if (j >= list.size) {
            j = 0
        }
        val bx = list[j]
        val by = list[j + 1]
        if (rayCrossesSegment(px, py, ax, ay, bx, by)) {
            crossings++
        }
    }

    // odd number of crossings?
    return crossings % 2 == 1
}

fun polygonContains(list: DoubleArray, px: Double, py: Double): Boolean {
    // ray casting algorithm http://rosettacode.org/wiki/Ray-casting_algorithm
    var crossings = 0

    // for each edge
    for (i in list.indices step 2) {
        val ax = list[i]
        val ay = list[i + 1]
        var j = i + 2
        //to close the last edge, you have to take the first point of your polygon
        if (j >= list.size) {
            j = 0
        }
        val bx = list[j]
        val by = list[j + 1]
        if (rayCrossesSegment(px, py, ax, ay, bx, by)) {
            crossings++
        }
    }

    // odd number of crossings?
    return crossings % 2 == 1
}

private fun rayCrossesSegment(point: PointD, a: PointD, b: PointD): Boolean {
    // Ray Casting algorithm checks, for each segment, if the point is
    // 1) to the left of the segment and
    // 2) not above nor below the segment. If these two conditions are met, it returns true
    var px: Double = point.x
    var py: Double = point.y
    var ax: Double = a.x
    var ay: Double = a.y
    var bx: Double = b.x
    var by: Double = b.y
    if (ay > by) {
        ax = b.x
        ay = b.y
        bx = a.x
        by = a.y
    }
    // alter longitude to cater for 180 degree crossings
    if (px < 0 || ax < 0 || bx < 0) {
        px += 360.0
        ax += 360.0
        bx += 360.0
    }
    // if the point has the same latitude as a or b, increase slightly py
    if (py == ay || py == by) py += 0.00000001


    // if the point is above, below or to the right of the segment, it returns false
    return if (py > by || py < ay || px > ax.coerceAtLeast(bx)) {
        false
    } else if (px < ax.coerceAtMost(bx)) {
        true
    } else {
        val red = if (ax != bx) (by - ay) / (bx - ax) else java.lang.Double.POSITIVE_INFINITY
        val blue = if (ax != px) (py - ay) / (px - ax) else java.lang.Double.POSITIVE_INFINITY
        blue >= red
    }
}

private fun rayCrossesSegment(
    px: Float,
    py: Float,
    ax: Float,
    ay: Float,
    bx: Float,
    by: Float,
): Boolean {
    // Ray Casting algorithm checks, for each segment, if the point is
    // 1) to the left of the segment and
    // 2) not above nor below the segment. If these two conditions are met, it returns true
    if (ay > by)
        return rayCrossesSegment(px, py, bx, by, ax, ay)

    // alter longitude to cater for 180 degree crossings
    if (px < 0 || ax < 0 || bx < 0)
        return rayCrossesSegment(px + 360f, py, ax + 360f, ay, bx + 360f, by)

    // if the point has the same latitude as a or b, increase slightly py
    if (py == ay || py == by)
        return rayCrossesSegment(px, py + 0.00001f, ax, ay, bx, by)

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

private fun rayCrossesSegment(
    px: Double,
    py: Double,
    ax: Double,
    ay: Double,
    bx: Double,
    by: Double,
): Boolean {
    // Ray Casting algorithm checks, for each segment, if the point is
    // 1) to the left of the segment and
    // 2) not above nor below the segment. If these two conditions are met, it returns true
    if (ay > by)
        return rayCrossesSegment(px, py, bx, by, ax, ay)

    // alter longitude to cater for 180 degree crossings
    if (px < 0 || ax < 0 || bx < 0)
        return rayCrossesSegment(px + 360f, py, ax + 360f, ay, bx + 360f, by)

    // if the point has the same latitude as a or b, increase slightly py
    if (py == ay || py == by)
        return rayCrossesSegment(px, py + 0.00001f, ax, ay, bx, by)

    // if the point is above, below or to the right of the segment, it returns false
    return if (py > by || py < ay || px > ax.coerceAtLeast(bx)) {
        false
    } else if (px < ax.coerceAtMost(bx)) {
        true
    } else {
        val red = if (ax != bx) (by - ay) / (bx - ax) else java.lang.Double.POSITIVE_INFINITY
        val blue = if (ax != px) (py - ay) / (px - ax) else java.lang.Double.POSITIVE_INFINITY
        blue >= red
    }
}

val Double.pow2: Double
    get() = this * this

fun findIntersection(l1s: PointF, l1e: PointF, l2s: PointF, l2e: PointF): PointF {
    val a1 = l1e.y - l1s.y
    val b1 = l1s.x - l1e.x
    val c1 = a1 * l1s.x + b1 * l1s.y

    val a2 = l2e.y - l2s.y
    val b2 = l2s.x - l2e.x
    val c2 = a2 * l2s.x + b2 * l2s.y

    val delta = a1 * b2 - a2 * b1

    return if (delta == 0f) {
        l1e
    } else {
        PointF((b2 * c1 - b1 * c2) / delta, (a1 * c2 - a2 * c1) / delta)
    }
}

fun findIntersection(l1s: PointD, l1e: PointD, l2s: PointD, l2e: PointD): PointD {
    val a1 = l1e.y - l1s.y
    val b1 = l1s.x - l1e.x
    val c1 = a1 * l1s.x + b1 * l1s.y

    val a2 = l2e.y - l2s.y
    val b2 = l2s.x - l2e.x
    val c2 = a2 * l2s.x + b2 * l2s.y

    val delta = a1 * b2 - a2 * b1
    return if (delta == 0.0) {
        l1e
    } else {
        PointD((b2 * c1 - b1 * c2) / delta, (a1 * c2 - a2 * c1) / delta)
    }
}
