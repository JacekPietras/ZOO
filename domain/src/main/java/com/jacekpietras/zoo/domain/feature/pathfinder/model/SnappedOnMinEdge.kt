package com.jacekpietras.zoo.domain.feature.pathfinder.model

import com.jacekpietras.geometry.PointD

internal sealed class SnappedOnMin {

    internal class SnappedOnMinEdge(
        val point: PointD,
        val edge: MinEdge,
        val ratio: Double,
    ) : SnappedOnMin()

    internal class SnappedOnMinNode(
        val node: MinNode,
    ) : SnappedOnMin()
}
