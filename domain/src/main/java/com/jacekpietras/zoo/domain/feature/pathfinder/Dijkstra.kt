package com.jacekpietras.zoo.domain.feature.pathfinder

import com.jacekpietras.zoo.domain.feature.pathfinder.model.Edge
import com.jacekpietras.zoo.domain.feature.pathfinder.model.Node
import com.jacekpietras.zoo.domain.feature.pathfinder.model.SnappedOn
import com.jacekpietras.zoo.domain.feature.pathfinder.model.SnappedOn.SnappedOnEdge
import com.jacekpietras.zoo.domain.feature.pathfinder.model.SnappedOn.SnappedOnNode
import java.util.PriorityQueue

internal class Dijkstra(
    private val vertices: Set<Node>,
    private val technicalAllowed: Boolean = false,
) {

    private val queue = PriorityQueue(10, comparator)
    private lateinit var costs: MutableMap<Node, Double>
    private val previous = mutableMapOf<Node, Node?>()
    private var outsideTechnical = false

    // subset of vertices, for which we know true distance
    private val visited = mutableSetOf<Node>()

    fun calculate(
        start: SnappedOn,
        end: SnappedOn,
    ): List<Node> {
        when (start) {
            is SnappedOnEdge -> {
                initQueueWithEndsOfStartingEdge(start)
                if (isCommonEdge(start, end)) {
                    addsToQueueConnectionOnSameEdge(start, end)
                }
            }
            is SnappedOnNode -> {
                costs = mutableMapOf(start.node to 0.0)
                queue.add(start.node to 0.0)
            }
        }

        return when (end) {
            is SnappedOnEdge -> {
                if (isCommonEdge(end, start)) {
                    addsToQueueConnectionOnSameEdge(start, end)
                }

                val endNode = end.toNode()
                val technical = endNode.edges.first().technical
                val endingEdge1 = Edge(
                    node = endNode,
                    technical = technical,
                    weight = haversine(end.point, end.near1.point),
                )
                val endingEdge2 = Edge(
                    node = endNode,
                    technical = technical,
                    weight = haversine(end.point, end.near2.point),
                )

                runAlgorithm(
                    endNode,
                    EndingEdge(end.near1, endingEdge1),
                    EndingEdge(end.near2, endingEdge2),
                )
            }
            is SnappedOnNode -> {
                runAlgorithm(end.node)
            }
        }
    }

    private fun SnappedOn.toNode(): Node =
        when (this) {
            is SnappedOnEdge -> {
                val node = Node(point)
                val technical = near1.edges.first { it.node == near2 }.technical
                val endingEdge1 = Edge(
                    node = near1,
                    technical = technical,
                    weight = haversine(point, near1.point),
                )
                val endingEdge2 = Edge(
                    node = near2,
                    technical = technical,
                    weight = haversine(point, near2.point),
                )
                node.edges.add(endingEdge1)
                node.edges.add(endingEdge2)
                node
            }
            is SnappedOnNode -> {
                node
            }
        }

    private fun isCommonEdge(
        snapStart: SnappedOnEdge,
        snapEnd: SnappedOn
    ): Boolean =
        when (snapEnd) {
            is SnappedOnEdge -> (snapStart.near1 == snapEnd.near1 && snapStart.near2 == snapEnd.near2) ||
                    (snapStart.near1 == snapEnd.near2 && snapStart.near2 == snapEnd.near1)
            is SnappedOnNode -> snapStart.near1 == snapEnd.node || snapStart.near2 == snapEnd.node
        }

    private fun addsToQueueConnectionOnSameEdge(
        startSnap: SnappedOn,
        endSnap: SnappedOn,
    ) {
        val costToEndOnSameEdge = haversine(startSnap.point, endSnap.point)

        val startNode = startSnap.toNode()
        val endNode = endSnap.toNode()

        costs[endNode] = costToEndOnSameEdge
        queue.add(endNode to costToEndOnSameEdge)
        previous[endNode] = startNode
    }

    private fun initQueueWithEndsOfStartingEdge(start: SnappedOnEdge) {
        val costToStart1 = haversine(start.point, start.near1.point)
        val costToStart2 = haversine(start.point, start.near2.point)
        val startNode = start.toNode()

        costs = mutableMapOf(
            start.near1 to costToStart1,
            start.near2 to costToStart2,
        )

        queue.add(start.near1 to costToStart1)
        queue.add(start.near2 to costToStart2)
        previous[start.near1] = startNode
        previous[start.near2] = startNode
    }

    private fun runAlgorithm(
        end: Node,
        endingEdge1: EndingEdge? = null,
        endingEdge2: EndingEdge? = null,
    ): List<Node> {
        if (technicalAllowed) {
            withTechnical(end, endingEdge1, endingEdge2)
        } else {
            withoutTechnical(end, endingEdge1, endingEdge2)
        }
        return getPath(end)
    }

    private fun withTechnical(
        end: Node,
        endingEdge1: EndingEdge?,
        endingEdge2: EndingEdge?,
    ) {
        while (visited.size != vertices.size) {
            // closest vertex that has not yet been visited
            if (queue.isEmpty()) return
            val (v: Node, distanceToV) = queue.remove()

            v.edges.forEach { neighbor ->
                runForEdgeWithTechnical(v, neighbor, distanceToV)
            }
            endingEdge1?.run {
                if (from == v) {
                    runForEdgeWithTechnical(v, edge, distanceToV)
                }
            }
            endingEdge2?.run {
                if (from == v) {
                    runForEdgeWithTechnical(v, edge, distanceToV)
                }
            }

            if (v == end) break

            visited.add(v)
        }
    }

    private fun withoutTechnical(
        end: Node,
        endingEdge1: EndingEdge?,
        endingEdge2: EndingEdge?,
    ) {
        while (visited.size != vertices.size) {
            // closest vertex that has not yet been visited
            if (queue.isEmpty()) return
            val (v: Node, distanceToV) = queue.remove()

            v.edges.forEach { neighbor ->
                runForEdgeWithoutTechnical(v, neighbor, distanceToV)
            }
            endingEdge1?.run {
                if (from == v) {
                    runForEdgeWithTechnical(v, edge, distanceToV)
                }
            }
            endingEdge2?.run {
                if (from == v) {
                    runForEdgeWithTechnical(v, edge, distanceToV)
                }
            }

            if (v == end) break

            visited.add(v)
        }
    }

    private fun runForEdgeWithTechnical(
        v: Node,
        neighbor: Edge,
        distanceToV: Double,
    ) {
        if (neighbor.node !in visited) {
            val newCost = distanceToV + neighbor.weight

            if (newCost < (costs[neighbor.node] ?: Double.MAX_VALUE)) {
                costs[neighbor.node] = newCost
                previous[neighbor.node] = v
                queue.add(neighbor.node to newCost)
            }
        }
    }

    private fun runForEdgeWithoutTechnical(
        v: Node,
        neighbor: Edge,
        distanceToV: Double,
    ) {
        if (neighbor.node !in visited && (!outsideTechnical || !neighbor.technical)) {
            if (!neighbor.technical) {
                outsideTechnical = true
            }

            val newCost = distanceToV + neighbor.weight

            if (newCost < (costs[neighbor.node] ?: Double.MAX_VALUE)) {
                costs[neighbor.node] = newCost
                previous[neighbor.node] = v
                queue.add(neighbor.node to newCost)
            }
        }
    }

    private fun getPath(
        end: Node,
    ): List<Node> {
        var current = end
        val result = mutableListOf(end)
        while (true) {
            val path = previous[current] ?: return result.reversed()
            if (path === current) return result.reversed()
            result.add(path)
            current = path
        }
    }

    private companion object {

        val comparator = Comparator<Pair<Node, Double>> { o1, o2 ->
            o1.second.compareTo(o2.second)
        }
    }

    private class EndingEdge(
        val from: Node,
        val edge: Edge,
    )
}
