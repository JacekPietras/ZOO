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
import kotlin.math.min

internal class MinDijkstra(
    private val vertices: Collection<MinNode>,
    private val technicalAllowed: Boolean = false,
) {

    private val queue: PriorityQueue<Pair<MinNode, Double>> = PriorityQueue(10, comparator)
    private lateinit var costs: MutableMap<MinNode, Double>
    private val previous = mutableMapOf<MinNode, MinEdge?>()
    private var outsideTechnical = false
    private val snapper = PointSnapper()

    // subset of vertices, for which we know true distance
    private val visited = mutableSetOf<MinNode>()

    fun calculate(
        start: SnappedOnMin,
        end: SnappedOnMin,
    ): List<PointD> {
        if (start == end) {
            return listOf(start.asNode().point)
        }
        when (start) {
            is SnappedOnMinEdge -> {
                initQueueWithEndsOfStartingEdge(start)
                val commonEdge = findCommonEdge(start, end)
                if (commonEdge != null) {
                    addsToQueueConnectionOnSameEdge(start, end, commonEdge)
                } else if (onReversedEdge(start, end)) {
                    throw IllegalStateException("With current implementation of snapping, shouldn't happen, might implement in future")
                }
            }
            is SnappedOnMinNode -> {
                costs = mutableMapOf(start.node to 0.0)
                queue.add(start.node to 0.0)
            }
        }
        if (end is SnappedOnMinEdge) {
            val commonEdge = findCommonEdge(end, start)
            if (commonEdge != null) {
                addsToQueueConnectionOnSameEdge(start, end, commonEdge)
            } else if (onReversedEdge(end, start)) {
                throw IllegalStateException("With current implementation of snapping, shouldn't happen, might implement in future")
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
        startSnap: SnappedOnMin,
        endSnap: SnappedOnMin,
        edgeSnap: MinEdge,
    ) {
        var start: SnappedOnMinEdge = snapper.getSnappedOnMinEdgeOnly(edgeSnap, startSnap.asNode().point)
        var end: SnappedOnMinEdge = snapper.getSnappedOnMinEdgeOnly(edgeSnap, endSnap.asNode().point)
        var costToEndOnSameEdge = abs(start.weightFromStart - end.weightFromStart)
        if (edgeSnap.node == edgeSnap.from) {
            if (endSnap is SnappedOnMinNode && endSnap.node.point == edgeSnap.from.point) {
                val start2: SnappedOnMinEdge = start.reversed()
                val end2: SnappedOnMinEdge = end.reversed().copy(weightFromStart = end.weightFromStart)
                val costToEndOnSameEdge2 = abs(start2.weightFromStart - end2.weightFromStart)
                if (costToEndOnSameEdge2 < costToEndOnSameEdge) {
                    start = start2
                    end = end2
                    costToEndOnSameEdge = costToEndOnSameEdge2
                }
            }
            if (startSnap is SnappedOnMinNode && startSnap.node.point == edgeSnap.from.point) {
                val start2: SnappedOnMinEdge = start.reversed().copy(weightFromStart = start.weightFromStart)
                val end2: SnappedOnMinEdge = end.reversed()
                val costToEndOnSameEdge2 = abs(start2.weightFromStart - end2.weightFromStart)
                if (costToEndOnSameEdge2 < costToEndOnSameEdge) {
                    start = start2
                    end = end2
                    costToEndOnSameEdge = costToEndOnSameEdge2
                }
            }
        }

        val startNode = MinNode(start.point)
        val endNode = MinNode(end.point)
        val edge = MinEdge(
            from = startNode,
            node = endNode,
            technical = end.edge.technical,
            weight = costToEndOnSameEdge,
            backward = false,
            corners = if (start.weightFromStart < end.weightFromStart) {
                start.edge.corners
                    .filter { (_, weight) -> start.weightFromStart < weight && weight < end.weightFromStart }
                    .map { (point, weight) -> point to (weight - start.weightFromStart) }
            } else {
                start.edge.corners
                    .filter { (_, weight) -> end.weightFromStart < weight && weight < start.weightFromStart }
                    .map { (point, weight) -> point to (weight - end.weightFromStart) }
            },
        )

        costs[endNode] = costToEndOnSameEdge
        queue.add(endNode to costToEndOnSameEdge)
        previous[endNode] = edge
    }

    private fun initQueueWithEndsOfStartingEdge(start: SnappedOnMinEdge) {
        val costToStart1 = start.weightFromStart
        val costToStart2 = (start.edge.weight - start.weightFromStart)

        if (start.edge.from == start.edge.node) {
            costs = mutableMapOf(
                start.edge.from to min(costToStart1, costToStart2)
            )
            queue.add(start.edge.from to min(costToStart1, costToStart2))
        } else {
            costs = mutableMapOf(
                start.edge.from to costToStart1,
                start.edge.node to costToStart2,
            )

            queue.add(start.edge.from to costToStart1)
            queue.add(start.edge.node to costToStart2)
        }
    }

    private fun findCommonEdge(
        snapStart: SnappedOnMinEdge,
        snapEnd: SnappedOnMin
    ): MinEdge? {
        return when (snapEnd) {
            is SnappedOnMinEdge -> if (snapStart.edge == snapEnd.edge) snapStart.edge else null
            is SnappedOnMinNode -> snapEnd.node.edges.find { edge -> edge.node.point == snapStart.point || edge.corners.any { it.first == snapStart.point } }
        }
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
    ): List<PointD> {
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
            if (queue.isEmpty()) return
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

    private fun pathTo(end: MinNode): List<PointD> {
        var current = end
        val result = mutableListOf(end.point)
        while (true) {
            val edgeToCurrent = previous[current] ?: return result.reversed()
            if (edgeToCurrent.from === current) return result.reversed()
            result.addAll(edgeToCurrent.corners.map(Pair<PointD, Double>::first).reversed())
            result.add(edgeToCurrent.from.point)
            current = edgeToCurrent.from
        }
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
