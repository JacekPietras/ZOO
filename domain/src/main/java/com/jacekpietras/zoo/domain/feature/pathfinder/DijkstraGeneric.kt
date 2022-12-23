package com.jacekpietras.zoo.domain.feature.pathfinder

internal class DijkstraGeneric<T>(
    vertices: Set<T>,
    private val start: T,
    private val end: T,
    edges: (T) -> List<T>,
    private val isTechnical: (T) -> Boolean,
    private val weight: (T, T) -> Double,
    technicalAllowed: Boolean = false,
) {

    private val previous: MutableMap<T, T?> = vertices.associateWith { null }.toMutableMap()

    init {
//        val pq =  PriorityQueue<Node>(vertices.size)

        // shortest distances
        val delta = vertices.associateWith { Double.MAX_VALUE }.toMutableMap()
        delta[start] = 0.0

        // subset of vertices, for which we know true distance
        val s = mutableSetOf<T>()

        var outsideTechnical = technicalAllowed

        while (s.size != vertices.size) {
            // closest vertex that has not yet been visited
            val (v: T, distanceToV) = delta
                .asSequence()
                .filter { !s.contains(it.key) }
                .minByOrNull(Map.Entry<T, Double>::value)
                .let(::checkNotNull)

            edges(v).forEach { neighbor ->
                if (neighbor !in s &&
                    (technicalAllowed || !outsideTechnical || !isTechnical(neighbor))
                ) {
                    if (!isTechnical(neighbor)) {
                        outsideTechnical = true
                    }

                    val newPath = distanceToV + weight(v, neighbor)

                    if (newPath < delta.getValue(neighbor)) {
                        delta[neighbor] = newPath
                        previous[neighbor] = v
                    }
                }
            }

            if (v == end) break

            v.let(s::add)
        }
    }

    fun getPath(): List<T> =
        pathTo(start, end)

    private fun pathTo(start: T, end: T): List<T> {
        val path = previous[end] ?: return listOf(end)
        return pathTo(start, path) + end
    }
}
