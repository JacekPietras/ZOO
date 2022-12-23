package com.jacekpietras.zoo.domain.feature.pathfinder

import com.jacekpietras.zoo.domain.feature.pathfinder.model.Node

internal class Dijkstra(
    vertices: Set<Node>,
    private val start: Node,
    private val end: Node,
    technicalAllowed: Boolean = false,
) {

    private val previous: MutableMap<Node, Node?> = vertices.associateWith { null }.toMutableMap()

    init {
        // shortest distances
        val delta = vertices.associateWith { Double.MAX_VALUE }.toMutableMap()
        delta[start] = 0.0

        // subset of vertices, for which we know true distance
        val s = mutableSetOf<Node>()

        var outsideTechnical = technicalAllowed

        while (s.size != vertices.size) {
            // closest vertex that has not yet been visited
            val (v: Node, distanceToV) = delta
                .asSequence()
                .filter { !s.contains(it.key) }
                .minByOrNull(Map.Entry<Node, Double>::value)
                .let(::checkNotNull)

            v.edges.forEach { neighbor ->
                if (neighbor.node !in s &&
                    (technicalAllowed || !outsideTechnical || !neighbor.technical)
                ) {
                    if (!neighbor.technical) {
                        outsideTechnical = true
                    }

                    val newPath = distanceToV + neighbor.weight

                    if (newPath < delta.getValue(neighbor.node)) {
                        delta[neighbor.node] = newPath
                        previous[neighbor.node] = v
                    }
                }
            }

            if (v == end) break

            v.let(s::add)
        }
    }

    fun getPath(): List<Node> =
        pathTo(start, end)

    private fun pathTo(start: Node, end: Node): List<Node> {
        val path = previous[end] ?: return listOf(end)
        return pathTo(start, path) + end
    }
}

