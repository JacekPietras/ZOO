package com.jacekpietras.zoo.domain.feature.pathfinder.model

import com.jacekpietras.geometry.PointD

internal class MinEdge(
    val node: MinNode,
    val from: MinNode,
    val technical: Boolean,
    val weight: Double,
    val backward: Boolean,
    val corners: List<Pair<PointD, Double>>,
){

    override fun toString() = "edge to (${node.x}, ${node.y}) | mid ${corners.map(Pair<PointD, Double>::first)},"
}
