package com.jacekpietras.zoo.domain.model

import com.jacekpietras.core.PointD

data class VisitedRoadEdge(
    val from: PointD,
    val to: PointD,
    val visited: DoubleArray,
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as VisitedRoadEdge

        if (from != other.from) return false
        if (to != other.to) return false
        if (!visited.contentEquals(other.visited)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = from.hashCode()
        result = 31 * result + to.hashCode()
        result = 31 * result + visited.contentHashCode()
        return result
    }
}
