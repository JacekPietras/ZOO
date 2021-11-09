package com.jacekpietras.zoo.domain.business

import com.jacekpietras.core.PointD
import com.jacekpietras.core.haversine
import com.jacekpietras.core.pow2

internal class Snapper {

    fun getSnappedEdge(
        nodes: Iterable<Node>,
        source: PointD,
        technicalAllowed: Boolean,
    ): Snapped {
        var result: Snapped? = null
        var shortest: Double = Double.MAX_VALUE

        if (nodes.none()) {
            throw IllegalStateException("Cannot find snapped edge for empty nodes")
        }

        nodes.forAllEdges { p1, p2, technical ->
            if (technicalAllowed || !technical) {
                val found = getSnappedToEdge(source, p1.point, p2.point)
                val foundToSource = haversine(source.x, source.y, found.x, found.y)
                if (foundToSource < shortest) {
                    shortest = foundToSource
                    result = Snapped(found, p1, p2)
                }
            }
        }

        return requireNotNull(result)
    }

    fun getSnappedPoint(nodes: Iterable<Node>, source: PointD): PointD {
        var result: PointD? = null
        var shortest: Double = Double.MAX_VALUE

        nodes.forAllEdges { p1, p2, _ ->
            val found = getSnappedToEdge(source, p1.point, p2.point)
            val foundToSource = haversine(source.x, source.y, found.x, found.y)
            if (foundToSource < shortest) {
                shortest = foundToSource
                result = found
            }
        }
        return result!!
    }

    private fun getSnappedToEdge(source: PointD, p1: PointD, p2: PointD): PointD {
        val u = ((source.x - p1.x) * (p2.x - p1.x) + (source.y - p1.y) * (p2.y - p1.y)) /
                ((p2.x - p1.x).pow2 + (p2.y - p1.y).pow2)

        return when {
            u < 0 -> p1
            u > 1 -> p2
            else -> PointD(
                p1.x + u * (p2.x - p1.x),
                p1.y + u * (p2.y - p1.y),
            )
        }
    }

    class Snapped(val point: PointD, val near1: Node, val near2: Node)
}