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

    private val queue: PriorityQueue<Pair<MinNode, Double>> = PriorityQueue(10, comparator)
    private lateinit var costs: MutableMap<MinNode, Double>
    private val previous = mutableMapOf<MinNode, MinEdge?>()
    private var outsideTechnical = false

    // subset of vertices, for which we know true distance
    private val visited = mutableSetOf<MinNode>()

    fun calculate(
        start: SnappedOnMin,
        end: SnappedOnMin,
    ): List<MinNode> {
        if (start == end) {
            return listOf(start.asNode())
        }
        when (start) {
            is SnappedOnMinEdge -> {
                initQueueWithEndsOfStartingEdge(start)

                if (onSameEdge(start, end)) {
                    addsToQueueConnectionOnSameEdge(start, end)
                } else if (onReversedEdge(start, end)) {
                    throw IllegalStateException("With current implementation of snapping, shouldn't happen, might implement in future")
                }
            }
            is SnappedOnMinNode -> {
                costs = mutableMapOf(start.node to 0.0)
                queue.add(start.node to 0.0)
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

    private fun addsToQueueConnectionOnSameEdge(
        start: SnappedOnMinEdge,
        end: SnappedOnMinEdge
    ) {
        val costToEndOnSameEdge = abs(start.weightFromStart - end.weightFromStart)
        val startNode = MinNode(start.point)
        val endNode = MinNode(end.point)
        val edge = MinEdge(
            from = startNode,
            node = endNode,
            technical = end.edge.technical,
            weight = costToEndOnSameEdge,
            backward = false,
            corners = start.edge.corners
                .filter { (_, weight) -> start.weightFromStart < weight && weight < end.weightFromStart }
                .map { (point, weight) -> point to (weight - start.weightFromStart) },
        )

        costs[endNode] = costToEndOnSameEdge
        queue.add(endNode to costToEndOnSameEdge)
        previous[endNode] = edge
    }

    private fun initQueueWithEndsOfStartingEdge(start: SnappedOnMinEdge) {
        val costToStart1 = start.weightFromStart
        val costToStart2 = (start.edge.weight - start.weightFromStart)

        costs = mutableMapOf(
            start.edge.from to costToStart1,
            start.edge.node to costToStart2,
        )

        queue.add(start.edge.from to costToStart1)
        queue.add(start.edge.node to costToStart2)
    }

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
        while (visited.size != vertices.size) {
            // closest vertex that has not yet been visited
            val (v, distanceToV) = queue.remove()

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

            visited.add(v)
        }
    }

    private fun searchForEndingWithoutTechnical(
        end: MinNode,
        endingEdge1: MinEdge? = null,
        endingEdge2: MinEdge? = null,
    ) {
        while (visited.size != vertices.size) {
            // closest vertex that has not yet been visited
            val (v, distanceToV) = queue.remove()

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

            visited.add(v)
        }
    }

    private fun runForEdgeWithTechnical(
        neighbor: MinEdge,
        distanceToV: Double,
    ) {
        if (neighbor.node !in visited) {
            val newCost = distanceToV + neighbor.weight

            if (newCost < (costs[neighbor.node] ?: Double.MAX_VALUE)) {
                costs[neighbor.node] = newCost
                previous[neighbor.node] = neighbor
                queue.add(neighbor.node to newCost)
            }
        }
    }

    private fun runForEdgeWithoutTechnical(
        neighbor: MinEdge,
        distanceToV: Double,
    ) {
        if (neighbor.node !in visited && (!outsideTechnical || !neighbor.technical)) {
            if (!neighbor.technical) {
                outsideTechnical = true
            }

            val newCost = distanceToV + neighbor.weight

            if (newCost < (costs[neighbor.node] ?: Double.MAX_VALUE)) {
                costs[neighbor.node] = newCost
                previous[neighbor.node] = neighbor
                queue.add(neighbor.node to newCost)
            }
        }
    }

    private fun pathTo(end: MinNode): List<MinNode> {
//        var current = end
//        val result = mutableListOf(end)
//        while (true) {
//            val edgeToCurrent = previous[current]?.from ?: return result.reversed()
//            result.add(edgeToCurrent)
//            current = edgeToCurrent
//        }
        val path = previous[end]?.from ?: return listOf(end)
        if (path === end) return listOf(end)
        return pathTo(path) + end
    }

    private fun SnappedOnMin.asNode(): MinNode =
        when (this) {
            is SnappedOnMinEdge -> MinNode(point)
            is SnappedOnMinNode -> node
        }

    private companion object {

        val comparator = Comparator<Pair<MinNode, Double>> { o1, o2 ->
            o1.second.compareTo(o2.second)
        }
    }
}
