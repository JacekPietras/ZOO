package com.jacekpietras.zoo.domain.business

import com.jacekpietras.core.PointD
import com.jacekpietras.core.pow2
import com.jacekpietras.zoo.domain.business.model.Node
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

internal fun cartesian(a: Node, b: Node): Double =
    sqrt((a.x - b.x).pow2 + (a.y - b.y).pow2)

internal fun cartesian(a: PointD, b: PointD): Double =
    sqrt((a.x - b.x).pow2 + (a.y - b.y).pow2)
