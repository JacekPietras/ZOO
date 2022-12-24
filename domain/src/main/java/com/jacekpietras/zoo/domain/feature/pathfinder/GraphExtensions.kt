package com.jacekpietras.zoo.domain.feature.pathfinder

import com.jacekpietras.geometry.PointD
import com.jacekpietras.geometry.pow2
import com.jacekpietras.zoo.domain.feature.pathfinder.model.Node
import kotlin.math.sqrt

internal fun Iterable<Node>.forAllEdges(block: (Node, Node, Boolean) -> Unit) {
    forEach { node ->
        node.edges.forEach { edge ->
            if (!edge.backward) {
                block(node, edge.node, edge.technical)
            }
        }
    }
}

internal fun Iterable<Node>.forAllEdges(block: (Node, Node, Boolean, Double) -> Unit) {
    forEach { node ->
        node.edges.forEach { edge ->
            if (!edge.backward) {
                block(node, edge.node, edge.technical, edge.weight)
            }
        }
    }
}

internal fun cartesian(a: Node, b: Node): Double =
    sqrt((a.x - b.x).pow2 + (a.y - b.y).pow2)

internal fun cartesian(a: PointD, b: PointD): Double =
    sqrt((a.x - b.x).pow2 + (a.y - b.y).pow2)
