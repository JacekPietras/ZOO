package com.jacekpietras.zoo.domain.business

internal class Dijkstra(
    vertices: Set<Node>,
    private val start: Node,
    private val end: Node,
    technicalAllowed: Boolean = false,
) {

    private val previous: MutableMap<Node, Node?> = vertices.map { it to null }.toMutableMap()

    init {
        // shortest distances
        val delta = vertices.map { it to Double.MAX_VALUE }.toMutableMap()
        delta[start] = 0.0

        // subset of vertices, for which we know true distance
        val s: MutableSet<Node> = mutableSetOf()

        var outsideTechnical = technicalAllowed

        while (s != vertices) {
            // closest vertex that has not yet been visited
            val v: Node = delta
                .asSequence()
                .filter { !s.contains(it.key) }
                .minByOrNull { it.value }!!
                .key

            v.edges.forEach { neighbor ->
                if (neighbor.node !in s &&
                    (technicalAllowed || !outsideTechnical || !neighbor.technical)
                ) {
                    val newPath = delta.getValue(v) + neighbor.length

                    if (!neighbor.technical) {
                        outsideTechnical = true
                    }

                    if (newPath < delta.getValue(neighbor.node)) {
                        delta[neighbor.node] = newPath
                        previous[neighbor.node] = v
                    }
                }
            }

            if (v == end) break

            s.add(v)
        }
    }

    private fun pathTo(start: Node, end: Node): List<Node> {
        val path = previous[end] ?: return listOf(end)
        return listOf(pathTo(start, path), listOf(end)).flatten()
    }

    fun getPath(): List<Node> {
        return pathTo(start, end)
    }

    private fun <T, R> List<Pair<T, R>>.toMutableMap(): MutableMap<T, R> =
        this.toMap().toMutableMap()
}

