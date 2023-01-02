package com.jacekpietras.zoo.domain.feature.pathfinder

import com.jacekpietras.geometry.PointD
import com.jacekpietras.zoo.domain.feature.pathfinder.model.MinEdge
import com.jacekpietras.zoo.domain.feature.pathfinder.model.MinNode
import com.jacekpietras.zoo.domain.feature.pathfinder.model.SnappedOnMin
import com.jacekpietras.zoo.domain.feature.pathfinder.model.SnappedOnMin.SnappedOnMinEdge
import com.jacekpietras.zoo.domain.feature.pathfinder.model.SnappedOnMin.SnappedOnMinNode
import java.util.PriorityQueue
import kotlin.contracts.contract
import kotlin.math.abs

internal class MinDijkstra(
    private val vertices: Collection<MinNode>,
    private val technicalAllowed: Boolean = false,
) {

    private val q: PriorityQueue<Pair<MinNode, Double>> = PriorityQueue(10, comparator)
    private lateinit var costs: MutableMap<MinNode, Double>
    private val previous = mutableMapOf<MinNode, MinNode?>()
    private var outsideTechnical = false

    // subset of vertices, for which we know true distance
    private val s = mutableSetOf<MinNode>()

    fun calculate(
        start: SnappedOnMin,
        end: SnappedOnMin,
    ): List<MinNode> {
        when (start) {
            is SnappedOnMinEdge -> {
                val costToStart1 = start.weightFromStart
                val costToStart2 = (start.edge.weight - start.weightFromStart)

                costs = mutableMapOf(
                    start.edge.from to costToStart1,
                    start.edge.node to costToStart2,
                )

                q.add(start.edge.from to costToStart1)
                q.add(start.edge.node to costToStart2)

                if (onSameEdge(start, end)) {
                    val costToEndOnSameEdge = abs(start.weightFromStart - end.weightFromStart)
                    val startNode = MinNode(start.point)
                    val endNode = MinNode(end.point)
                    costs[endNode] = costToEndOnSameEdge
                    q.add(endNode to costToEndOnSameEdge)
                    previous[endNode] = startNode
                } else if (onReversedEdge(start, end)) {
                    throw IllegalStateException("With current implementation of snapping, shouldn't happen, might implement in future")
                }

//                val pointsBeforeSnapped = listOf(start.edge.from) +
//                        start.edge.corners
//                            .filter { (_, weight) -> weight <= start.weightFromStart }
//                            .map { (p, _) -> MinNode(p) }
//                val pointsAfterSnapped = start.edge.corners
//                    .filter { (_, weight) -> weight >= start.weightFromStart }
//                    .map { (p, _) -> MinNode(p) } +
//                        start.edge.node
//
//                pointsBeforeSnapped.zipWithNext { a, b -> previous[a] = b }
//                pointsAfterSnapped.zipWithNext { a, b -> previous[b] = a }

//                if (start.point != pointsBeforeSnapped.last().point)
//                    previous[pointsBeforeSnapped.last()] = MinNode(start.point)
//                if (start.point != pointsAfterSnapped.first().point)
//                    previous[pointsAfterSnapped.first()] = MinNode(start.point)
            }
            is SnappedOnMinNode -> {
                costs = mutableMapOf(start.node to 0.0)

                q.add(start.node to 0.0)
            }
        }

        return when (end) {
            is SnappedOnMinEdge -> {
                val endNode = MinNode(end.point)
                val endingEdge1 = MinEdge(
                    from = end.edge.from,
                    node = endNode,
                    technical = end.edge.technical,
                    weight = end.weightFromStart,
                    backward = false,
                    corners = emptyList(),
                )
                val endingEdge2 = MinEdge(
                    from = end.edge.node,
                    node = endNode,
                    technical = end.edge.technical,
                    weight = end.edge.weight - end.weightFromStart,
                    backward = false,
                    corners = emptyList(),
                )
                runAlgorithm(
                    endNode,
                    endingEdge1,
                    endingEdge2,
                )
            }
            is SnappedOnMinNode -> {
                runAlgorithm(end.node)
            }
        }
    }

//    private fun cornersBetweenSnaps(
//        snapStart: SnappedOnMinEdge,
//        snapEnd: SnappedOnMinEdge
//    ): List<PointD>  =
//        if (snapStart.weightFromStart < snapEnd.weightFromStart) {
//            snapStart.edge.corners
//                .filter { (_, weight) -> snapStart.weightFromStart < weight && weight < snapEnd.weightFromStart }
//        } else {
//            snapStart.edge.corners
//                .filter { (_, weight) -> snapEnd.weightFromStart < weight && weight < snapStart.weightFromStart }
//                .reversed()
//        }
//            .map { (p, _) -> p }

    private fun onSameEdge(
        snapStart: SnappedOnMinEdge,
        snapEnd: SnappedOnMin
    ): Boolean {
        contract {
            returns(true) implies (snapEnd is SnappedOnMinEdge)
        }
        return snapEnd is SnappedOnMinEdge && snapStart.edge == snapEnd.edge
    }

    private fun onReversedEdge(
        snapStart: SnappedOnMinEdge,
        snapEnd: SnappedOnMin
    ): Boolean {
        contract {
            returns(true) implies (snapEnd is SnappedOnMinEdge)
        }
        return snapEnd is SnappedOnMinEdge &&
                snapStart.edge.from == snapEnd.edge.node && snapStart.edge.node == snapEnd.edge.from &&
                snapStart.cornerPoints == snapEnd.cornerPoints.reversed()
    }

    private val SnappedOnMinEdge.cornerPoints
        get() = edge.corners.map(Pair<PointD, Double>::first)

    private fun runAlgorithm(
        end: MinNode,
        endingEdge1: MinEdge? = null,
        endingEdge2: MinEdge? = null,
    ): List<MinNode> {
        if (technicalAllowed) {
            searchForEndingWithTechnical(end, endingEdge1, endingEdge2)
        } else {
            searchForEndingWithoutTechnical(end, endingEdge1, endingEdge2)
        }
        return pathTo(end)
    }

    private fun searchForEndingWithTechnical(
        end: MinNode,
        endingEdge1: MinEdge? = null,
        endingEdge2: MinEdge? = null,
    ) {
        while (s.size != vertices.size) {
            // closest vertex that has not yet been visited
            val (v, distanceToV) = q.remove()

            v.edges.forEach { neighbor ->
                runForEdgeWithTechnical(neighbor, distanceToV)
            }
            endingEdge1?.run {
                if (this.from == v) {
                    runForEdgeWithTechnical(this, distanceToV)
                }
            }
            endingEdge2?.run {
                if (this.from == v) {
                    runForEdgeWithTechnical(this, distanceToV)
                }
            }

            if (v == end) break

            s.add(v)
        }
    }

    private fun searchForEndingWithoutTechnical(
        end: MinNode,
        endingEdge1: MinEdge? = null,
        endingEdge2: MinEdge? = null,
    ) {
        while (s.size != vertices.size) {
            // closest vertex that has not yet been visited
            val (v, distanceToV) = q.remove()

            v.edges.forEach { neighbor ->
                runForEdgeWithoutTechnical(neighbor, distanceToV)
            }
            endingEdge1?.run {
                if (this.from == v) {
                    runForEdgeWithoutTechnical(this, distanceToV)
                }
            }
            endingEdge2?.run {
                if (this.from == v) {
                    runForEdgeWithoutTechnical(this, distanceToV)
                }
            }

            if (v == end) break

            s.add(v)
        }
    }

    private fun runForEdgeWithTechnical(
        neighbor: MinEdge,
        distanceToV: Double,
    ) {
        if (neighbor.node !in s) {
            val newCost = distanceToV + neighbor.weight

            if (newCost < (costs[neighbor.node] ?: Double.MAX_VALUE)) {
                costs[neighbor.node] = newCost
                previous[neighbor.node] = neighbor.from
                q.add(neighbor.node to newCost)
            }
        }
    }

    private fun runForEdgeWithoutTechnical(
        neighbor: MinEdge,
        distanceToV: Double,
    ) {
        if (neighbor.node !in s && (!outsideTechnical || !neighbor.technical)) {
            if (!neighbor.technical) {
                outsideTechnical = true
            }

            val newCost = distanceToV + neighbor.weight

            if (newCost < (costs[neighbor.node] ?: Double.MAX_VALUE)) {
                costs[neighbor.node] = newCost
                previous[neighbor.node] = neighbor.from
                q.add(neighbor.node to newCost)
            }
        }
    }

    private fun pathTo(end: MinNode): List<MinNode> {
        val path = previous[end] ?: return listOf(end)
        return pathTo(path) + end
    }

    private companion object {

        val comparator = Comparator<Pair<MinNode, Double>> { o1, o2 ->
            o1.second.compareTo(o2.second)
        }
    }
}
