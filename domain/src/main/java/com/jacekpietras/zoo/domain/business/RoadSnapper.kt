@file:Suppress("ComplexRedundantLet")

package com.jacekpietras.zoo.domain.business

import com.jacekpietras.core.PointD
import com.jacekpietras.core.haversine
import com.jacekpietras.zoo.domain.model.MapItemEntity
import com.jacekpietras.zoo.domain.model.SnappedOnEdge
import com.jacekpietras.zoo.domain.model.VisitedRoadEdge
import kotlin.math.max
import kotlin.math.min

internal class RoadSnapper {

    suspend fun run(list: List<MapItemEntity.PathEntity>): List<VisitedRoadEdge> =
        list
            .map { path ->
                path.vertices
                    .snapPointsToRoad()
                    .fillMissingCorners()
            }
            .flatten()
            .connectPointsIntoEdges()
            .normalizeEdgeDirection()
            .mergeVisitedParts()
            .toVisitedEdges()

    private suspend fun List<PointD>.snapPointsToRoad(): List<SnappedOnEdge> =
        map { GraphAnalyzer.getSnappedPointOnEdge(it, false) }

    private suspend fun List<SnappedOnEdge>.fillMissingCorners(): List<List<SnappedOnEdge>> =
        zipWithNext()
            .mapNotNull { (start, end) ->
                when {
                    start onSameEdge end -> listOf(start, end)
                    commonNode(start, end) != null -> {
                        val commonNode = checkNotNull(commonNode(start, end))
                        listOf(start, SnappedOnEdge(commonNode.point, commonNode, commonNode), end)
                    }
                    else -> {
                        GraphAnalyzer.getShortestPathWithContext(
                            startPoint = start,
                            endPoint = end,
                        )
                            .filterOutNotFoundRoutes()
                            ?.filterCrossingTechnical()
                            ?.filterOutLongerThan(length = 30)
                            ?.map { SnappedOnEdge(it.point, it, it) }
                            ?.mapIndexed { i, it ->
                                when (it.point) {
                                    start.point -> start
                                    end.point -> end
                                    else -> it
                                }
                            }
                    }
                }
            }
            .filter { it.isNotEmpty() }
            .let(::connectIfPossible)
            .map { it.filterWithPrev { prev, next -> prev.point != next.point } }

    internal fun connectIfPossible(source: List<List<SnappedOnEdge>>): List<List<SnappedOnEdge>> {
        val result = mutableListOf<List<SnappedOnEdge>>()
        var temp = mutableListOf<SnappedOnEdge>()

        source.forEach { list ->
            when {
                temp.isEmpty() -> {
                    temp.addAll(list)
                }
                temp.last().point == list.first().point -> {
                    temp.addAll(list.subList(1, list.size))
                }
                commonNode(temp.last(), list.first()) != null -> {
                    val commonNode = checkNotNull(commonNode(temp.last(), list.first()))
                    temp.add(SnappedOnEdge(commonNode.point, commonNode, commonNode))
                    temp.addAll(list)
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

    private fun commonNode(left: SnappedOnEdge, right: SnappedOnEdge): Node? {
        if (left.near1 == right.near1) return left.near1
        if (left.near1 == right.near2) return left.near1
        if (left.near2 == right.near1) return left.near2
        if (left.near2 == right.near2) return left.near2
        return null
    }

    private fun List<Node>.filterCrossingTechnical(): List<Node>? {
        zipWithNext { prev, next ->
            prev.edges.firstOrNull { it.node == next && !it.technical } ?: return null
        }
        return this
    }

    private fun List<Node>.filterOutLongerThan(length: Int): List<Node>? {
        val sum = zipWithNext { prev, next -> haversine(prev.point.x, prev.point.y, next.point.x, next.point.y) }
            .sum()
        return this.takeIf { sum < length }
    }

    private fun List<Node>.filterOutNotFoundRoutes(): List<Node>? =
        takeIf { it.size != 1 }

    private fun List<List<SnappedOnEdge>>.connectPointsIntoEdges(): List<VisitedRoadEdgePart> =
        map { continuous ->
            continuous.zipWithNext { prev, next ->
                val nodes = (prev.getUniqueNodes() + next.getUniqueNodes()).toList()
                check(nodes.size == 2) { "there is no line between $prev -> $next" }

                val diff = nodes[1].x - nodes[0].x
                val prevPercent = (prev.point.x - nodes[0].x) / diff
                val nextPercent = (next.point.x - nodes[0].x) / diff

                check(prevPercent in 0.0..1.0)
                check(nextPercent in 0.0..1.0)

                VisitedRoadEdgePart(
                    from = nodes[0].point,
                    to = nodes[1].point,
                    range = min(prevPercent, nextPercent)..max(prevPercent, nextPercent),
                )
            }
        }.flatten()

    private fun List<VisitedRoadEdgePart>.normalizeEdgeDirection() =
        map {
            if ((it.from.x > it.to.x) ||
                (it.from.x == it.to.x && it.from.y > it.to.y)
            ) {
                it.reversed()
            } else {
                it
            }
        }

    private fun List<VisitedRoadEdgePart>.mergeVisitedParts(): List<Pair<Pair<PointD, PointD>, Intervals<Double>>> =
        groupBy { it.from to it.to }
            .mapValues {
                it.value
                    .fold(Intervals<Double>()) { acc, v -> acc + v.range }
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

    private class VisitedRoadEdgePart(
        val from: PointD,
        val to: PointD,
        val range: ClosedRange<Double>,
    ) {

        fun reversed() = VisitedRoadEdgePart(
            from = to,
            to = from,
            range = (1 - range.endInclusive)..(1 - range.start),
        )
    }
}

internal fun <T> List<T>.filterWithPrev(condition: (T, T) -> Boolean): List<T> {
    var prev: T? = null
    return filter { next ->
        (prev?.let { condition(it, next) } ?: true)
            .also { prev = next }
    }
}