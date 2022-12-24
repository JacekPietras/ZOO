package com.jacekpietras.zoo.domain.feature.pathfinder.model

import com.jacekpietras.geometry.PointD

internal class MinEdge(
    val node: MinNode,
    val technical: Boolean,
    var weight: Double = 0.0,
    var midPoints: List<PointD> = mutableListOf(),
){

    override fun toString() = "edge to (${node.x}, ${node.y}) | mid $midPoints,"
}
