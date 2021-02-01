package com.jacekpietras.zoo.map.model

import android.graphics.*
import com.jacekpietras.zoo.map.containsLine

internal class PolygonF(val list: List<PointF>) {

    constructor(vararg points: Pair<Float, Float>)
            : this(points.map { PointF(it.first, it.second) })

    fun intersects(rect: RectF): Boolean {
        if (contains(PointF(rect.left, rect.top))) return true
        list.zipWithNext().forEach {
            if (rect.containsLine(it.first, it.second)) return true
        }
        return false
    }

    fun contains(point: PointF): Boolean {
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
}

internal fun Canvas.drawPath(path: PolygonF, paint: Paint) {
    val toDraw = Path()
    path.list.forEachIndexed { i, point ->
        if (i == 0) {
            toDraw.moveTo(point.x, point.y)
        } else {
            toDraw.lineTo(point.x, point.y)
        }
    }
    drawPath(toDraw, paint)
}