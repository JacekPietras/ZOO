package com.jacekpietras.zoo.domain.feature.pathfinder.model

internal class Edge(
    val node: Node,
    val technical: Boolean,
    val backward: Boolean = false,
    var weight: Double = 0.0,
) {
    val x: Double
        get() = node.x

    val y: Double
        get() = node.y
}