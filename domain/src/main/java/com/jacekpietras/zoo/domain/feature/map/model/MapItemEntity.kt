package com.jacekpietras.zoo.domain.feature.map.model

import com.jacekpietras.core.PointD
import com.jacekpietras.zoo.domain.feature.centerpoint.CenterPointFinder

sealed class MapItemEntity {

    data class PolygonEntity(
        val vertices: List<PointD>
    ) : MapItemEntity() {

        fun findCenter(): PointD =
            CenterPointFinder.findCenter(vertices)

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
