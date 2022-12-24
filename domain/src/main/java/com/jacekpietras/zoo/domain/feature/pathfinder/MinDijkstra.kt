package com.jacekpietras.zoo.domain.feature.pathfinder

import com.jacekpietras.zoo.domain.feature.pathfinder.model.MinNode
import com.jacekpietras.zoo.domain.feature.pathfinder.model.Node
import java.util.PriorityQueue

internal class MinDijkstra(
    private val vertices: Collection<MinNode>,
    private val start: MinNode,
    private val end: MinNode,
    technicalAllowed: Boolean = false,
) {

    private val previous = mutableMapOf<MinNode, MinNode?>()

    init {
        if (technicalAllowed) {
            withTechnical()
        } else {
            withoutTechnical()
        }
    }

    private fun withTechnical() {
        // shortest distances
        val delta = mutableMapOf(start to 0.0)

        val q = PriorityQueue(10, comparator)
        q.add(start to 0.0)

        // subset of vertices, for which we know true distance
        val s = mutableSetOf<MinNode>()

        while (s.size != vertices.size) {
            // closest vertex that has not yet been visited
            val (v: MinNode, distanceToV) = q.remove()

            v.edges.forEach { neighbor ->
                if (neighbor.node !in s) {
                    val newCost = distanceToV + neighbor.weight

                    if (newCost < (delta[neighbor.node] ?: Double.MAX_VALUE)) {
                        delta[neighbor.node] = newCost
                        previous[neighbor.node] = v
                        q.add(neighbor.node to newCost)
                    }
                }
            }

            if (v == end) break

            s.add(v)
        }
    }

    private fun withoutTechnical() {
        // shortest distances
        val delta = mutableMapOf(start to 0.0)

        val q = PriorityQueue(10, comparator)
        q.add(start to 0.0)

        // subset of vertices, for which we know true distance
        val s = mutableSetOf<MinNode>()

        var outsideTechnical = false

        while (s.size != vertices.size) {
            // closest vertex that has not yet been visited
            val (v: MinNode, distanceToV) = q.remove()

            v.edges.forEach { neighbor ->
                if (neighbor.node !in s && (!outsideTechnical || !neighbor.technical)) {
                    if (!neighbor.technical) {
                        outsideTechnical = true
                    }

                    val newCost = distanceToV + neighbor.weight

                    if (newCost < (delta[neighbor.node] ?: Double.MAX_VALUE)) {
                        delta[neighbor.node] = newCost
                        previous[neighbor.node] = v
                        q.add(neighbor.node to newCost)
                    }
                }
            }

            if (v == end) break

            s.add(v)
        }
    }

    fun getPath(): List<MinNode> =
        pathTo(start, end)

    private fun pathTo(start: MinNode, end: MinNode): List<MinNode> {
        val path = previous[end] ?: return listOf(end)
        return pathTo(start, path) + end
    }

    private companion object {

        val comparator = Comparator<Pair<MinNode, Double>> { o1, o2 ->
            o1.second.compareTo(o2.second)
        }
    }
}
