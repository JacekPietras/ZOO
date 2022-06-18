package com.jacekpietras.zoo.domain.model

import com.jacekpietras.core.PointD
import com.jacekpietras.zoo.domain.polylabel.PolyLabel

sealed class MapItemEntity {

    data class PolygonEntity(
        val vertices: List<PointD>
    ) : MapItemEntity() {

        fun findCenter(): PointD =
            if (vertices.size <= 4) {
                val x = vertices.sumOf { it.x } / vertices.size
                val y = vertices.sumOf { it.y } / vertices.size
                PointD(x, y)
            } else {
                val request = arrayOf(vertices.map { arrayOf(it.x, it.y) }.toTypedArray())
                PolyLabel.polyLabel(request, 0.0000001)
                    .let { PointD(it.coordinates[0], it.coordinates[1]) }
            }

        constructor(vararg pairs: Pair<Number, Number>)
                : this(pairs.asIterable().map { PointD(it.first.toDouble(), it.second.toDouble()) })
    }

    data class PathEntity(
        val vertices: List<PointD>
    ) : MapItemEntity() {

        constructor(vararg pairs: Pair<Number, Number>)
                : this(pairs.asIterable().map { PointD(it.first.toDouble(), it.second.toDouble()) })
    }
}
