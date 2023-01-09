package com.jacekpietras.zoo.domain.feature.pathfinder.model

import com.jacekpietras.geometry.PointD

sealed class SnappedOn(
    open val point: PointD,
) {

    internal data class SnappedOnEdge(
        override val point: PointD,
        val near1: Node,
        val near2: Node,
    ) : SnappedOn(point) {

        infix fun onSameEdgeWith(right: SnappedOnEdge): Boolean =
            (this.near1 == right.near1 && this.near2 == right.near2) ||
                    (this.near1 == right.near2 && this.near2 == right.near1)

        fun getUniqueNodes(): Set<Node> =
            when (point) {
                near1.point -> setOf(near1)
                near2.point -> setOf(near2)
                else -> setOf(near1, near2)
            }

        infix fun hasCommonNodeWith(right: SnappedOnEdge): Boolean =
            commonNode(right) != null

        fun commonNode(right: SnappedOnEdge): Node? {
            if (this.near1 == right.near1) return this.near1
            if (this.near1 == right.near2) return this.near1
            if (this.near2 == right.near1) return this.near2
            if (this.near2 == right.near2) return this.near2
            return null
        }
    }

    internal data class SnappedOnNode(
        val node: Node,
    ) : SnappedOn(node.point)
}