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

    override fun toString() = "(${from.x}, ${from.y}) -> ${corners.map(Pair<PointD, Double>::first)} -> (${node.x}, ${node.y}),"

    fun reversed() =        MinEdge(
             node = from,
     from = node,
     technical=technical,
     weight=weight,
     backward = !backward,
     corners = corners.reversed().map { (p,w) -> p to weight - w },
    )
}
