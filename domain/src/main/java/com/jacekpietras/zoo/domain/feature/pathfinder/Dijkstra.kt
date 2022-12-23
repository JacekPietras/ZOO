package com.jacekpietras.zoo.domain.feature.pathfinder

import com.jacekpietras.zoo.domain.feature.pathfinder.model.Node
import java.util.PriorityQueue

internal class Dijkstra(
    private val vertices: Set<Node>,
    private val start: Node,
    private val end: Node,
    technicalAllowed: Boolean = false,
) {

    private val previous = mutableMapOf<Node, Node?>()

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

        val q = PriorityQueue(vertices.size / 2, comparator)
        q.add(start to 0.0)

        // subset of vertices, for which we know true distance
        val s = mutableSetOf<Node>()

        while (s.size != vertices.size) {
            // closest vertex that has not yet been visited
            val (v: Node, distanceToV) = q.remove()

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

        val q = PriorityQueue(vertices.size / 2, comparator)
        q.add(start to 0.0)

        // subset of vertices, for which we know true distance
        val s = mutableSetOf<Node>()

        var outsideTechnical = false

        while (s.size != vertices.size) {
            // closest vertex that has not yet been visited
            val (v: Node, distanceToV) = q.remove()

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

    fun getPath(): List<Node> =
        pathTo(start, end)

    private fun pathTo(start: Node, end: Node): List<Node> {
        val path = previous[end] ?: return listOf(end)
        return pathTo(start, path) + end
    }

    private companion object {

        val comparator = Comparator<Pair<Node, Double>> { o1, o2 ->
            o1.second.compareTo(o2.second)
        }
    }
}
