package com.jacekpietras.zoo.domain.business.model

internal class Edge(
    val node: Node,
    val technical: Boolean,
    val backward: Boolean,
    var length: Double = 0.0,
) {
    val x: Double
        get() = node.x

    val y: Double
        get() = node.y
}