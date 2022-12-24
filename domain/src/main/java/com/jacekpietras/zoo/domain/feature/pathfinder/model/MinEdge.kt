package com.jacekpietras.zoo.domain.feature.pathfinder.model

import com.jacekpietras.geometry.PointD

internal class MinEdge(
    val node: MinNode,
    val from: MinNode,
    val technical: Boolean,
    val weight: Double,
    val midPoints: List<PointD>,
){

    override fun toString() = "edge to (${node.x}, ${node.y}) | mid $midPoints,"
}
