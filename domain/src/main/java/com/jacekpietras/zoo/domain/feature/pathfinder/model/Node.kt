package com.jacekpietras.zoo.domain.feature.pathfinder.model

import com.jacekpietras.geometry.PointD
import com.jacekpietras.geometry.haversine

internal class Node(
    val point: PointD,
    val edges: MutableSet<Edge> = mutableSetOf()
) {

    val x: Double
        get() = point.x

    val y: Double
        get() = point.y

    fun connect(node: Node, technical: Boolean, backward: Boolean) {
        edges.add(Edge(node, technical, backward))
    }

    fun connectAndCalc(node: Node, technical: Boolean, backward: Boolean) {
        edges.add(Edge(node, technical, backward, haversine(this.x, this.y, node.x, node.y)))
    }

    fun disconnect(node: Node) {
        edges.remove(edges.first { it.node == node })
    }

    override fun hashCode(): Int =
        point.hashCode()

    override fun toString() = "Node($x, $y),"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Node

        if (point != other.point) return false

        return true
    }
}