package com.jacekpietras.zoo.domain.model

sealed class MapItemEntity {

    data class PolygonEntity(
        val vertices: List<LatLon>
    ) : MapItemEntity(){

        constructor(vararg pairs: Pair<Number, Number>)
                : this(pairs.asIterable().map { LatLon(it.first.toDouble(), it.second.toDouble()) })
    }

    data class PathEntity(
        val vertices: List<LatLon>
    ) : MapItemEntity() {

        constructor(vararg pairs: Pair<Number, Number>)
                : this(pairs.asIterable().map { LatLon(it.first.toDouble(), it.second.toDouble()) })
    }
}
