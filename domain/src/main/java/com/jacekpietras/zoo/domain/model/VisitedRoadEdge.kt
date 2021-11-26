package com.jacekpietras.zoo.domain.model

import com.jacekpietras.core.PointD


sealed class VisitedRoadEdge(
    open val from: PointD,
    open val to: PointD,
) {

    data class Partially(
        override val from: PointD,
        override val to: PointD,
        val visited: DoubleArray,
    ) : VisitedRoadEdge(from, to) {

        fun toPath(): List<MapItemEntity.PathEntity> {
            val result = mutableListOf<MapItemEntity.PathEntity>()
            for (i in 0 until (visited.size) step 2) {
                val diff = to - from
                val moveStart = diff * visited[i]
                val moveEnd = diff * visited[i + 1]

                result.add(
                    MapItemEntity.PathEntity(listOf(from + moveStart, from + moveEnd))
                )
            }
            return result
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as Partially

            if (from != other.from) return false
            if (to != other.to) return false
            if (!visited.contentEquals(other.visited)) return false

            return true
        }

        override fun hashCode(): Int {
            var result = from.hashCode()
            result = 31 * result + to.hashCode()
            result = 31 * result + visited.contentHashCode()
            return result
        }

        override fun toString(): String {
            val result = mutableListOf<String>()
            for (i in 0 until (visited.size) step 2) {
                result.add("%.6f".format(visited[i]) + "->" + "%.6f".format(visited[i + 1]))
            }
            return "[${from.x}, ${from.y}] -> [${to.x}, ${to.y}] || $result"
        }
    }

    data class Fully(
        override val from: PointD,
        override val to: PointD,
    ) : VisitedRoadEdge(from, to) {

        fun toPath(): MapItemEntity.PathEntity =
            MapItemEntity.PathEntity(listOf(from, to))

        override fun toString(): String {
            return "[${from.x}, ${from.y}] -> [${to.x}, ${to.y}] || Fully"
        }
    }
}
