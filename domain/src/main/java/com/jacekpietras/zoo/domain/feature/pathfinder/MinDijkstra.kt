package com.jacekpietras.zoo.domain.feature.pathfinder

import com.jacekpietras.zoo.domain.feature.pathfinder.model.MinNode
import com.jacekpietras.zoo.domain.feature.pathfinder.model.SnappedOnMin
import com.jacekpietras.zoo.domain.feature.pathfinder.model.SnappedOnMin.SnappedOnMinEdge
import com.jacekpietras.zoo.domain.feature.pathfinder.model.SnappedOnMin.SnappedOnMinNode
import java.util.PriorityQueue

internal class MinDijkstra(
    private val vertices: Collection<MinNode>,
    private val technicalAllowed: Boolean = false,
) {

    private val q: PriorityQueue<Pair<MinNode, Double>> = PriorityQueue(10, comparator)
    private lateinit var costs: MutableMap<MinNode, Double>
    private val previous = mutableMapOf<MinNode, MinNode?>()

    fun calculate(
        start: SnappedOnMin,
        end: MinNode,
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

                val pointsBeforeSnapped = listOf(start.edge.from) +
                        start.edge.corners
                            .filter { (_, weight) -> weight <= start.weightFromStart }
                            .map { (p, _) -> MinNode(p) }
                val pointsAfterSnapped = start.edge.corners
                    .filter { (_, weight) -> weight >= start.weightFromStart }
                    .map { (p, _) -> MinNode(p) } +
                        start.edge.node

                pointsBeforeSnapped.zipWithNext { a, b -> previous[a] = b }
                pointsAfterSnapped.zipWithNext { a, b -> previous[b] = a }
            }
            is SnappedOnMinNode -> {
                costs = mutableMapOf(start.node to 0.0)

                q.add(start.node to 0.0)
            }
        }

        return runAlgorithm(end)
    }

    private fun runAlgorithm(
        end: MinNode,
    ): List<MinNode> {
        if (technicalAllowed) {
            withTechnical(end)
        } else {
            withoutTechnical(end)
        }
        return pathTo(end)
    }

    private fun withTechnical(
        end: MinNode,
    ) {
        // subset of vertices, for which we know true distance
        val s = mutableSetOf<MinNode>()

        while (s.size != vertices.size) {
            // closest vertex that has not yet been visited
            val (v, distanceToV) = q.remove()

            v.edges.forEach { neighbor ->
                if (neighbor.node !in s) {
                    val newCost = distanceToV + neighbor.weight

                    if (newCost < (costs[neighbor.node] ?: Double.MAX_VALUE)) {
                        costs[neighbor.node] = newCost
                        previous[neighbor.node] = v
                        q.add(neighbor.node to newCost)
                    }
                }
            }

            if (v == end) break

            s.add(v)
        }
    }

    private fun withoutTechnical(
        end: MinNode,
    ) {
        // subset of vertices, for which we know true distance
        val s = mutableSetOf<MinNode>()

        var outsideTechnical = false

        while (s.size != vertices.size) {
            // closest vertex that has not yet been visited
            val (v, distanceToV) = q.remove()

            v.edges.forEach { neighbor ->
                if (neighbor.node !in s && (!outsideTechnical || !neighbor.technical)) {
                    if (!neighbor.technical) {
                        outsideTechnical = true
                    }

                    val newCost = distanceToV + neighbor.weight

                    if (newCost < (costs[neighbor.node] ?: Double.MAX_VALUE)) {
                        costs[neighbor.node] = newCost
                        previous[neighbor.node] = v
                        q.add(neighbor.node to newCost)
                    }
                }
            }

            if (v == end) break

            s.add(v)
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
