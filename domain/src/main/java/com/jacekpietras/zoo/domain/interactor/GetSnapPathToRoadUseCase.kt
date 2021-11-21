@file:Suppress("ComplexRedundantLet")

package com.jacekpietras.zoo.domain.interactor

import com.jacekpietras.core.PointD
import com.jacekpietras.zoo.domain.business.GraphAnalyzer
import com.jacekpietras.zoo.domain.model.MapItemEntity
import com.jacekpietras.zoo.domain.model.Snapped

class GetSnapPathToRoadUseCase(
) {

    private suspend fun snapToRoad(list: List<PointD>): List<Snapped> =
        list.map {
            GraphAnalyzer.getSnappedPointWithContext(it, false)
        }

    private suspend fun fillCorners(list: List<Snapped>): List<Snapped> =
        list
            .zipWithNext()
            .map { (start, end) ->
                if (start onSameEdge end) {
                    listOf(start)
                } else {
                    GraphAnalyzer.getShortestPathWithContext(
                        endPoint = end,
                        startPoint = start,
                    )
                        .filterNot { it.point == start.point || it.point == end.point }
                        .map { Snapped(it.point, it, it) }
                        .let {
                            it.toMutableList().apply {
                                add(0, start)
                                add(end)
                            }
                        }
                }
            }
            .flatten() + list.last()

    suspend fun run(list: List<MapItemEntity.PathEntity>): List<MapItemEntity.PathEntity> =
        list
            .map { path ->
                path.vertices
                    .let { snapToRoad(it) }
                    .let { fillCorners(it) }
                    .let { it.map { a -> a.point } }
                    .let { MapItemEntity.PathEntity(it) }
            }

    private infix fun Snapped.onSameEdge(right: Snapped): Boolean =
        (this.near1 == right.near1 && this.near2 == right.near2) ||
                (this.near1 == right.near2 && this.near2 == right.near1)
}
