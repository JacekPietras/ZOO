@file:Suppress("ComplexRedundantLet")

package com.jacekpietras.zoo.domain.business

import com.jacekpietras.core.PointD
import com.jacekpietras.zoo.domain.business.PathSnapper.VisitedRoadEdgePart
import com.jacekpietras.zoo.domain.model.MapItemEntity
import com.jacekpietras.zoo.domain.model.VisitedRoadEdge

internal class PathListSnapper {

    private val pathSnapper = PathSnapper()

    suspend fun snapToEdges(list: List<MapItemEntity.PathEntity>): List<VisitedRoadEdge> =
        list
            .map { pathSnapper.snapToEdgeParts(it) }
            .flatten()
            .mergeVisitedParts()
            .toVisitedEdges()

    @Suppress("unused")
    suspend fun snapToEdgesButLessEfficient(list: List<MapItemEntity.PathEntity>): List<VisitedRoadEdge> =
        list
            .map { pathSnapper.snapToEdges(it) }
            .flatten()
            .merge()

    private fun List<VisitedRoadEdgePart>.mergeVisitedParts(): List<Pair<Pair<PointD, PointD>, Intervals<Double>>> =
        groupBy { it.from to it.to }
            .mapValues {
                it.value.fold(Intervals<Double>()) { acc, v -> acc + v.range }
            }
            .toList()

    private fun List<Pair<Pair<PointD, PointD>, Intervals<Double>>>.toVisitedEdges(): List<VisitedRoadEdge> =
        map { (point, visited) ->
            if (visited.equals(0.0..0.1)) {
                VisitedRoadEdge.Fully(point.first, point.second)
            } else {
                VisitedRoadEdge.Partially(point.first, point.second, visited)
            }
        }

    private fun List<VisitedRoadEdge>.merge(): List<VisitedRoadEdge> =
        groupBy { it.from to it.to }
            .values
            .map { list ->
                list.drop(1).fold(list.first()) { acc, v ->
                    when {
                        acc is VisitedRoadEdge.Fully -> acc
                        v is VisitedRoadEdge.Fully -> v
                        else -> {
                            val visited = (acc as VisitedRoadEdge.Partially).visited + (v as VisitedRoadEdge.Partially).visited

                            if (visited.equals(0.0..0.1)) {
                                VisitedRoadEdge.Fully(acc.from, acc.to)
                            } else {
                                VisitedRoadEdge.Partially(acc.from, acc.to, visited)
                            }
                        }
                    }
                }
            }

    fun add(list: List<VisitedRoadEdge>, from: PointD, to: PointD, range: ClosedRange<Double>): List<VisitedRoadEdge> {
        val found = list.find { it.from == from && it.to == to }
        if (found != null) {
            return list.replace(found, found.add(range))
        }

        val foundReversed = list.find { it.from == to && it.to == from }
        if (foundReversed != null) {
            return list.replace(foundReversed, foundReversed.add((1 - range.endInclusive)..(1 - range.start)))
        }

        val newEdge = if (range == 0.0..0.1) {
            VisitedRoadEdge.Fully(from, to)
        } else {
            VisitedRoadEdge.Partially(from, to, Intervals(range))
        }

        return list + newEdge
    }

    private fun VisitedRoadEdge.add(range: ClosedRange<Double>): VisitedRoadEdge =
        if (this is VisitedRoadEdge.Fully) {
            this
        } else {
            val visited = (this as VisitedRoadEdge.Partially).visited + range

            if (visited.equals(0.0..0.1)) {
                VisitedRoadEdge.Fully(from, to)
            } else {
                VisitedRoadEdge.Partially(from, to, visited)
            }
        }

    private fun <T> List<T>.replace(prev: T, next: T): List<T> =
        map {
            if (it === prev) {
                next
            } else {
                it
            }
        }
}
