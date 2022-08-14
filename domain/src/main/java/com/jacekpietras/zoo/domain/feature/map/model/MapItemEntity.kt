package com.jacekpietras.zoo.domain.feature.map.model

import com.jacekpietras.geometry.PointD
import com.jacekpietras.zoo.domain.feature.centerpoint.CenterPointFinder

sealed class MapItemEntity {

    data class PolygonEntity(
        val vertices: List<PointD>
    ) : MapItemEntity() {

        private var cachedCenter: PointD? = null

        fun findCenter(): PointD =
            cachedCenter ?: (CenterPointFinder.findCenter(vertices).also { cachedCenter = it })

        fun left(): Double = vertices.minBy { it.x }.x

        fun right(): Double = vertices.maxBy { it.x }.x

        fun top(): Double = vertices.minBy { it.y }.y

        fun bottom(): Double = vertices.maxBy { it.y }.y

        fun width(): Double = right() - left()

        fun height(): Double = bottom() - top()

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
