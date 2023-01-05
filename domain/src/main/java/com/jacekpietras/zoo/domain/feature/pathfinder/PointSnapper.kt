package com.jacekpietras.zoo.domain.feature.pathfinder

import com.jacekpietras.geometry.PointD
import com.jacekpietras.geometry.haversine
import com.jacekpietras.geometry.pow2
import com.jacekpietras.zoo.domain.feature.pathfinder.model.MinEdge
import com.jacekpietras.zoo.domain.feature.pathfinder.model.MinNode
import com.jacekpietras.zoo.domain.feature.pathfinder.model.Node
import com.jacekpietras.zoo.domain.feature.pathfinder.model.SnappedOnEdge
import com.jacekpietras.zoo.domain.feature.pathfinder.model.SnappedOnMin
import com.jacekpietras.zoo.domain.feature.pathfinder.model.SnappedOnMin.SnappedOnMinEdge
import com.jacekpietras.zoo.domain.feature.pathfinder.model.SnappedOnMin.SnappedOnMinNode

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

    fun getSnappedOnMinEdges(
        nodes: Iterable<MinNode>,
        source: PointD,
        technicalAllowed: Boolean,
    ): SnappedOnMin {
        var result: Pair<SnappedOnMin, Double>? = null

        nodes.forAllMinEdges { edge ->
            if ((technicalAllowed || !edge.technical) && (result?.second ?: Double.MAX_VALUE) > 0.0 && edgesAreConnected(edge.from, edge.node)) {
                val snappedOnEdge = getSnappedOnMinEdge(edge, source)
                if ((snappedOnEdge?.second ?: Double.MAX_VALUE) < (result?.second ?: Double.MAX_VALUE)) {
                    result = snappedOnEdge
                }
            }
        }

        return checkNotNull(result?.first)
    }

    fun getSnappedOnMinEdge(
        edge: MinEdge,
        source: PointD,
    ): Pair<SnappedOnMin, Double>? {
        var result: Pair<SnappedOnMin, Double>? = null

        if (edge.node.point == source) {
            result = SnappedOnMinNode(edge.node) to 0.0
        } else if (edge.from.point == source) {
            result = SnappedOnMinNode(edge.from) to 0.0
        } else {
            edge.forEdgeParts { p1, p2, weightBefore ->
                val found = getSnappedToEdge(source, p1, p2)
                val foundToSource = haversine(source.x, source.y, found.x, found.y)
                if (foundToSource < (result?.second ?: Double.MAX_VALUE)) {
                    val weightToP1 = haversine(p1.x, p1.y, found.x, found.y)
                    result = if (edge.node.point == found) {
                        SnappedOnMinNode(edge.node) to foundToSource
                    } else if (edge.from.point == found) {
                        SnappedOnMinNode(edge.from) to foundToSource
                    } else {
                        SnappedOnMinEdge(
                            point = found,
                            edge = edge,
                            weightFromStart = weightBefore + weightToP1,
                        ) to foundToSource
                    }
                }
            }
        }

        return result
    }

    fun getSnappedOnMinEdgeOnly(
        edge: MinEdge,
        source: PointD,
    ): SnappedOnMinEdge {
        var result: SnappedOnMinEdge? = null
        var shortest: Double = Double.MAX_VALUE

        edge.forEdgeParts { p1, p2, weightBefore ->
            val found = getSnappedToEdge(source, p1, p2)
            val foundToSource = haversine(source.x, source.y, found.x, found.y)
            if (foundToSource < shortest) {
                val weightToP1 = haversine(p1.x, p1.y, found.x, found.y)
                result = SnappedOnMinEdge(
                    point = found,
                    edge = edge,
                    weightFromStart = weightBefore + weightToP1,
                )
                shortest = foundToSource
            }
        }

        return checkNotNull(result)
    }

    private fun MinEdge.forEdgeParts(block: (p1: PointD, p2: PointD, weightBefore: Double) -> Unit) {
        val start = from.point to 0.0
        val end = node.point to weight
        (listOf(start) + corners + end).zipWithNext().forEach { (p1, p2) ->
            val (p1Point, p1Weight) = p1
            val (p2Point, _) = p2
            block(p1Point, p2Point, p1Weight)
        }
    }

    private fun edgesAreConnected(p1: Node, p2: Node) =
        p1.edges.any { it.node == p2 } && p2.edges.any { it.node == p1 }

    // todo check if necessary with extensive unit tests
    private fun edgesAreConnected(p1: MinNode, p2: MinNode) =
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
