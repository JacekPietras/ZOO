@file:Suppress("ComplexRedundantLet")

package com.jacekpietras.zoo.domain.interactor

import com.jacekpietras.core.PointD
import com.jacekpietras.core.haversine
import com.jacekpietras.zoo.domain.business.GraphAnalyzer
import com.jacekpietras.zoo.domain.business.Node
import com.jacekpietras.zoo.domain.model.MapItemEntity
import com.jacekpietras.zoo.domain.model.Snapped

class GetSnapPathToRoadUseCase {

    private suspend fun snapToRoad(list: List<PointD>): List<Snapped> =
        list.map {
            GraphAnalyzer.getSnappedPointWithContext(it, false)
        }

    private suspend fun fillCorners(list: List<Snapped>): List<List<Snapped>> =
        list
            .zipWithNext()
            .mapNotNull { (start, end) ->
                if (start onSameEdge end) {
                    listOf(start, end)
                } else {
                    GraphAnalyzer.getShortestPathWithContext(
                        startPoint = start,
                        endPoint = end,
                    )
                        .filterOutLongerThan(length = 30)
                        ?.filterNot { it.point == start.point || it.point == end.point }
                        ?.map { Snapped(it.point, it, it) }
                        ?.let {
                            it.toMutableList().apply {
                                add(0, start)
                                add(end)
                            }
                        }
                }
            }
            .filter { it.isNotEmpty() }
            .let(::connectIfPossible)

    internal fun connectIfPossible(source: List<List<Snapped>>): List<List<Snapped>> {
        val result = mutableListOf<List<Snapped>>()
        var temp = mutableListOf<Snapped>()

        source.forEach { list ->
            when {
                temp.isEmpty() -> {
                    temp.addAll(list)
                }
                temp.last().point == list.first().point -> {
                    temp.addAll(list.subList(1, list.size))
                }
                else -> {
                    result.add(temp)
                    temp = mutableListOf()
                    temp.addAll(list)
                }
            }
        }

        result.add(temp)

        return result
    }

    private fun List<Node>.filterOutLongerThan(length: Int): List<Node>? {
        val sum = zipWithNext { prev, next -> haversine(prev.point.x, prev.point.y, next.point.x, next.point.y) }
            .sum()
        return this.takeIf { sum < length }
    }

    suspend fun run(list: List<MapItemEntity.PathEntity>): List<MapItemEntity.PathEntity> =
        list
            .map { path ->
                path.vertices
                    .let { snapToRoad(it) }
                    .let { fillCorners(it) }
                    .map { it.map { a -> a.point } }
                    .map { MapItemEntity.PathEntity(it) }
            }
            .flatten()

    private infix fun Snapped.onSameEdge(right: Snapped): Boolean =
        (this.near1 == right.near1 && this.near2 == right.near2) ||
                (this.near1 == right.near2 && this.near2 == right.near1)
}
