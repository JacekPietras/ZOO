package com.jacekpietras.zoo.domain.feature.pathfinder

import com.jacekpietras.geometry.PointD
import com.jacekpietras.geometry.pow2
import com.jacekpietras.zoo.domain.feature.pathfinder.model.Node
import com.jacekpietras.zoo.domain.feature.pathfinder.model.SnappedOn
import com.jacekpietras.zoo.domain.feature.pathfinder.model.SnappedOn.SnappedOnEdge
import com.jacekpietras.zoo.domain.feature.pathfinder.model.SnappedOn.SnappedOnNode

internal class PointSnapper {

    fun getSnappedOn(
        nodes: Iterable<Node>,
        source: PointD,
        technicalAllowed: Boolean,
    ): SnappedOn {
        var result: SnappedOn? = null
        var shortest: Double = Double.MAX_VALUE

        nodes.forAllEdges { p1, p2, technical ->
            if (technicalAllowed || !technical) {
                if (p1.point == source) {
                    return SnappedOnNode(p1)
                }
                if (p2.point == source) {
                    return SnappedOnNode(p2)
                }
                val found = getSnappedToEdge(source, p1.point, p2.point)
                val foundToSource = haversine(source, found)
                if (foundToSource < shortest) {
                    if (p1.point == found) {
                        result = SnappedOnNode(p1)
                        shortest = foundToSource
                    } else if (p2.point == found) {
                        result = SnappedOnNode(p2)
                        shortest = foundToSource
                    } else {
                        result = SnappedOnEdge(found, p1, p2)
                        shortest = foundToSource
                    }
                    if (shortest == 0.0) {
                        return checkNotNull(result)
                    }
                }
            }
        }

        return checkNotNull(result)
    }

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
                val foundToSource = haversine(source, found)
                if (foundToSource < shortest) {
                    shortest = foundToSource
                    result = SnappedOnEdge(found, p1, p2)
                    if (shortest == 0.0) {
                        return checkNotNull(result)
                    }
                }
            }
        }

        return checkNotNull(result)
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
}
