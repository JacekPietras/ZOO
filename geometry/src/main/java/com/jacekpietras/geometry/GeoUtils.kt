package com.jacekpietras.geometry

import kotlin.math.asin
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

private const val EARTH_RADIUS = 6378000.1
private const val MAGIC = 1.6

// Distance in meters
fun haversine(p1x: Double, p1y: Double, p2x: Double, p2y: Double): Double {
    val dLat: Double = Math.toRadians(p2y - p1y)
    val dLon: Double = Math.toRadians(p2x - p1x)
    val lat1R = Math.toRadians(p1y)
    val lat2R = Math.toRadians(p2y)

    val a = sin(dLat * 0.5).pow2 + sin(dLon * 0.5).pow2 * cos(lat1R) * cos(lat2R)
    return 12745600 * asin(sqrt(a))
}

fun inflateLine(points: List<PointD>, size: Number): List<PointD> {
    val edges = points.zipWithNext() + points.reversed().zipWithNext()
    val edgesWithBearing = edges.map { (a, b) ->
        Triple(a, b, bearing(a, b) + 90)
    }
    return edgesWithBearing.map { (a, b, bearing) ->
        listOf(
            perpendicular(a, size, bearing),
            perpendicular(b, size, bearing),
        )
    }.flatten()
}

fun inflatePolygon(points: List<PointD>, size: Int): List<PointD> {
    val edges = (points + points[0]).zipWithNext()
    val edgesWithBearing = edges.map { (a, b) ->
        Triple(a, b, bearing(a, b) + 90)
    }
    return edgesWithBearing.map { (a, b, bearing) ->
        listOf(
            perpendicular(perpendicular(a, size, bearing), size, bearing + 90),
            perpendicular(perpendicular(b, size, bearing), size, bearing - 90),
        )
    }.flatten()
}

fun bearing(
    a: PointD,
    b: PointD,
): Double {
    val ax = Math.toRadians(a.x / MAGIC)
    val ay = Math.toRadians(a.y)
    val bx = Math.toRadians(b.x / MAGIC)
    val by = Math.toRadians(b.y)

    val y = sin(by - ay) * cos(bx)
    val x = cos(ax) * sin(bx) -
            sin(ax) * cos(bx) * cos(by - ay)
    val t = atan2(y, x)
    return (t * 180 / Math.PI + 360) % 360
}

fun perpendicular(center: PointD, distance: Number, b: Double): PointD {
    val d = distance.toDouble() / EARTH_RADIUS
    val brng = Math.toRadians(b)
    val lat1 = Math.toRadians(center.x / MAGIC)
    val lon1 = Math.toRadians(center.y)

    var x = asin(sin(lat1) * cos(d) + cos(lat1) * sin(d) * cos(brng))
    var y = lon1 + atan2(sin(brng) * sin(d) * cos(lat1), cos(d) - sin(lat1) * sin(x))

    x = Math.toDegrees(x) * MAGIC
    y = Math.toDegrees(y)

    return pointInDistance(
        begin = center,
        end = PointD(x, y),
        distance = distance,
    )
}

fun pointInDistance(begin: PointD, end: PointD, distance: Number): PointD {
    val pointDistance = haversine(begin.x, begin.y, end.x, end.y)
    return pointInPercent(
        begin = begin,
        end = end,
        percent = distance.toDouble() / pointDistance,
    )
}

fun pointInPercent(begin: PointD, end: PointD, percent: Double): PointD {
    val diff = end - begin
    return begin + (diff * percent)
}
