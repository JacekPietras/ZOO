package com.jacekpietras.zoo.domain.feature.pathfinder

import com.jacekpietras.geometry.PointD
import com.jacekpietras.geometry.haversine
import com.jacekpietras.geometry.pow2
import com.jacekpietras.zoo.domain.feature.pathfinder.model.Node
import com.jacekpietras.zoo.domain.feature.pathfinder.model.SnappedOnEdge

internal class PointSnapper {

    fun getSnappedOnEdge(
        nodes: Iterable<Node>,
        source: PointD,
        technicalAllowed: Boolean,
    ): SnappedOnEdge {
        var result: SnappedOnEdge? = null
        var shortest: Double = Double.MAX_VALUE

        nodes.forAllEdges { p1, p2, technical ->
            if (technicalAllowed || !technical) {
                val found = getSnappedToEdge(source, p1.point, p2.point)
                val foundToSource = haversine(source.x, source.y, found.x, found.y)
                if (foundToSource < shortest && edgesAreConnected(p1, p2)) {
                    shortest = foundToSource
                    result = SnappedOnEdge(found, p1, p2)
                }
            }
        }

        return requireNotNull(result)
    }

    private fun edgesAreConnected(p1: Node, p2: Node) =
        p1.edges.any { it.node == p2 } && p2.edges.any { it.node == p1 }

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
}
