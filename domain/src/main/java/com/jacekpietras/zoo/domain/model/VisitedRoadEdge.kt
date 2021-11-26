package com.jacekpietras.zoo.domain.model

import com.jacekpietras.core.PointD
import com.jacekpietras.zoo.domain.business.Intervals


sealed class VisitedRoadEdge(
    open val from: PointD,
    open val to: PointD,
) {

    data class Partially(
        override val from: PointD,
        override val to: PointD,
        val visited: Intervals<Double>,
    ) : VisitedRoadEdge(from, to) {

        fun toPath(): List<MapItemEntity.PathEntity> {
            val array = visited.toDoubleArray()
            return mutableListOf<MapItemEntity.PathEntity>().apply {
                for (i in 0 until (array.size) step 2) {
                    val diff = to - from
                    val moveStart = diff * array[i]
                    val moveEnd = diff * array[i + 1]

                    add(
                        MapItemEntity.PathEntity(listOf(from + moveStart, from + moveEnd))
                    )
                }
            }
        }

        override fun toString(): String {
            val result = mutableListOf<String>()
            val array = visited.toDoubleArray()
            for (i in 0 until (array.size) step 2) {
                result.add("%.6f".format(array[i]) + "->" + "%.6f".format(array[i + 1]))
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
