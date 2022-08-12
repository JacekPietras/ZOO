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
