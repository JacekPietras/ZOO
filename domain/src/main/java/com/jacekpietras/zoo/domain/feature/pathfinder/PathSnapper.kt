@file:Suppress("ComplexRedundantLet")

package com.jacekpietras.zoo.domain.feature.pathfinder

import com.jacekpietras.geometry.PointD
import com.jacekpietras.geometry.haversine
import com.jacekpietras.zoo.domain.feature.map.model.MapItemEntity
import com.jacekpietras.zoo.domain.feature.pathfinder.model.Intervals
import com.jacekpietras.zoo.domain.feature.pathfinder.model.Node
import com.jacekpietras.zoo.domain.feature.pathfinder.model.SnappedOnEdge
import com.jacekpietras.zoo.domain.model.VisitedRoadEdge
import kotlin.math.max
import kotlin.math.min

internal class PathSnapper(
    private val graphAnalyzer: GraphAnalyzer,
) {

    suspend fun snapToEdges(path: MapItemEntity.PathEntity): List<VisitedRoadEdge> =
        snapToEdgeParts(path)
            .mergeVisitedParts()
            .toVisitedEdges()

    suspend fun snapToEdgeParts(path: MapItemEntity.PathEntity): List<VisitedRoadEdgePart> =
        path.vertices
            .snapPointsToRoad()
            .fillMissingCorners()
            .connectIfPossible()
            .filterDuplicatesInRow()
            .connectPointsIntoEdges()
            .normalizeEdgeDirection()

    private suspend fun List<PointD>.snapPointsToRoad(): List<SnappedOnEdge> =
        map { graphAnalyzer.getSnappedPointOnEdge(it, false) }

    private suspend fun List<SnappedOnEdge>.fillMissingCorners(): List<List<SnappedOnEdge>> =
        zipWithNext { start, end ->
            when {
                start onSameEdgeWith end -> {
                    listOf(start, end)
                }
                start hasCommonNodeWith end -> {
                    val commonNode = checkNotNull(start.commonNode(end))
                    listOf(start, SnappedOnEdge(commonNode.point, commonNode, commonNode), end)
                }
                else -> {
                    graphAnalyzer.getShortestPathWithContext(
                        startPoint = start,
                        endPoint = end,
                    )
                        .filterOutNotFoundRoutes()
                        ?.filterCrossingTechnical()
                        ?.filterOutLongerThan(length = 30)
                        ?.map { SnappedOnEdge(it.point, it, it) }
                        ?.map {
                            when (it.point) {
                                start.point -> start
                                end.point -> end
                                else -> it
                            }
                        }
                }
            }
        }.filterNotNull().filter { it.isNotEmpty() }

    private fun List<Node>.filterOutNotFoundRoutes(): List<Node>? =
        takeIf { it.size != 1 }

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

    private fun List<List<SnappedOnEdge>>.connectIfPossible(): List<List<SnappedOnEdge>> {
        val result = mutableListOf<List<SnappedOnEdge>>()
        var temp = mutableListOf<SnappedOnEdge>()

        forEach { list ->
            when {
                temp.isEmpty() -> {
                    temp.addAll(list)
                }
                temp.last().point == list.first().point -> {
                    temp.addAll(list.subList(1, list.size))
                }
                temp.last() hasCommonNodeWith list.first() -> {
                    val commonNode = checkNotNull(temp.last().commonNode(list.first()))
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

    private fun List<List<SnappedOnEdge>>.filterDuplicatesInRow(): List<List<SnappedOnEdge>> =
        map { it.filterWithPrev { prev, next -> prev.point != next.point } }

    private fun getPercentOfSegment(begin: Node, end: Node, point: PointD): Double {
        val diffX = end.x - begin.x
        val percent = if (diffX != 0.0) {
            (point.x - begin.x) / diffX
        } else {
            val diffY = end.y - begin.y

            check(diffY != 0.0) { "there is no distance between $begin -> $end" }

            (point.y - begin.y) / diffY
        }
        check(percent in 0.0..1.0) { "percent value is $percent" }

        return percent
    }

    private fun List<List<SnappedOnEdge>>.connectPointsIntoEdges(): List<VisitedRoadEdgePart> =
        map { continuous ->
            continuous.zipWithNext { prev, next ->
                val nodes = (prev.getUniqueNodes() + next.getUniqueNodes()).toList()
                check(nodes.size == 2) { "there is no line between $prev -> $next" }

                val prevPercent = getPercentOfSegment(nodes[0], nodes[1], prev.point)
                val nextPercent = getPercentOfSegment(nodes[0], nodes[1], next.point)

                VisitedRoadEdgePart(
                    from = nodes[0].point,
                    to = nodes[1].point,
                    range = min(prevPercent, nextPercent)..max(prevPercent, nextPercent),
                )
            }
        }.flatten()

    private fun List<VisitedRoadEdgePart>.normalizeEdgeDirection(): List<VisitedRoadEdgePart> =
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

    internal class VisitedRoadEdgePart(
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