package com.jacekpietras.zoo.domain.business

internal class Dijkstra(
    vertices: Set<Node>,
    private val start: Node,
    private val end: Node,
) {

    private lateinit var shortestPathTree: Map<Node, Node?>

    init {

        // shortest distances
        val delta = vertices.map { it to Double.MAX_VALUE }.toMutableMap()

        // subset of vertices, for which we know true distance
        val s: MutableSet<Node> = mutableSetOf()

        delta[start] = 0.0

        val previous: MutableMap<Node, Node?> = vertices.map { it to null }.toMutableMap()

        while (s.size != vertices.size) {
            // closest vertex that has not yet been visited
            val v: Node = delta
                .filter { !s.contains(it.key) }
                .minByOrNull { it.value }!!
                .key

            v.edges
                .filter { it.node !in s }
                .forEach { neighbor ->
                    val newPath = delta.getValue(v) + neighbor.length

                    if (newPath < delta.getValue(neighbor.node)) {
                        delta[neighbor.node] = newPath
                        previous[neighbor.node] = v
                    }
                }

            if (v == end) {
                shortestPathTree = previous.toMap()
                break
            }

            s.add(v)
        }
    }

    private fun pathTo(start: Node, end: Node): List<Node> {
        val path = shortestPathTree[end] ?: return listOf(end)
        return listOf(pathTo(start, path), listOf(end)).flatten()
    }

    fun getPath(): List<Node> {
        return pathTo(start, end)
    }

    private fun <T, R> List<Pair<T, R>>.toMutableMap(): MutableMap<T, R> =
        this.toMap().toMutableMap()
}

