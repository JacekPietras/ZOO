package com.jacekpietras.zoo.domain.feature.pathfinder.model

import com.jacekpietras.geometry.PointD
import com.jacekpietras.geometry.haversine

internal class MinNode(
    val point: PointD,
    val edges: MutableSet<MinEdge> = mutableSetOf()
) {

    val x: Double
        get() = point.x

    val y: Double
        get() = point.y

    fun connect(node: MinNode, technical: Boolean, weight: Double) {
        edges.add(MinEdge(node, technical, weight))
    }

    override fun hashCode(): Int =
        point.hashCode()

    override fun toString() = "Node($x, $y),"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MinNode

        if (point != other.point) return false

        return true
    }
}