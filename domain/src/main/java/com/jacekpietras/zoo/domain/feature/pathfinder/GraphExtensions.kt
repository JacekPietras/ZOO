package com.jacekpietras.zoo.domain.feature.pathfinder

import com.jacekpietras.geometry.PointD
import com.jacekpietras.geometry.haversine
import com.jacekpietras.geometry.pow2
import com.jacekpietras.zoo.domain.feature.pathfinder.model.Node
import kotlin.math.sqrt

internal inline fun Iterable<Node>.forAllEdges(block: (p1: Node, p2: Node, technical: Boolean) -> Unit) {
    forEach { node ->
        node.edges.forEach { edge ->
            if (!edge.backward) {
                block(node, edge.node, edge.technical)
            }
        }
    }
}

internal fun Iterable<Node>.allEdges(): List<Triple<Node, Node, Boolean>> {
    val result = mutableListOf<Triple<Node, Node, Boolean>>()
    forAllEdges { node, node2, technical -> result.add(Triple(node, node2, technical)) }
    return result
}

internal fun cartesian(a: Node, b: Node): Double =
    sqrt((a.x - b.x).pow2 + (a.y - b.y).pow2)

internal fun cartesian(a: PointD, b: PointD): Double =
    sqrt((a.x - b.x).pow2 + (a.y - b.y).pow2)

internal fun haversine(a: PointD, b: PointD): Double =
    haversine(a.x, a.y, b.x, b.y)
