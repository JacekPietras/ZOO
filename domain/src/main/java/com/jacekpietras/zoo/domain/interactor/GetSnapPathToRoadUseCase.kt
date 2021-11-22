@file:Suppress("ComplexRedundantLet")

package com.jacekpietras.zoo.domain.interactor

import com.jacekpietras.core.PointD
import com.jacekpietras.core.haversine
import com.jacekpietras.zoo.domain.business.GraphAnalyzer
import com.jacekpietras.zoo.domain.business.Node
import com.jacekpietras.zoo.domain.model.MapItemEntity
import com.jacekpietras.zoo.domain.model.Snapped

class GetSnapPathToRoadUseCase(
    private val initializeGraphAnalyzerIfNeededUseCase: InitializeGraphAnalyzerIfNeededUseCase,
) {

    private suspend fun snapToRoad(list: List<PointD>): List<Snapped> =
        list.map {
            initializeGraphAnalyzerIfNeededUseCase.run()
            GraphAnalyzer.getSnappedPointWithContext(it, false)
        }

    private suspend fun fillCorners(list: List<Snapped>): List<List<Snapped>> =
        list
            .zipWithNext()
            .mapNotNull { (start, end) ->
                when {
                    start onSameEdge end -> listOf(start, end)
                    commonNode(start, end) != null -> {
                        val commonNode = checkNotNull(commonNode(start, end))
                        listOf(start, Snapped(commonNode.point, commonNode, commonNode), end)
                    }
                    else -> {
                        GraphAnalyzer.getShortestPathWithContext(
                            startPoint = start,
                            endPoint = end,
                        )
                            .filterOutNotFoundRoutes()
                            ?.filterCrossingTechnical()
                            ?.filterOutLongerThan(length = 30)
                            ?.map { Snapped(it.point, it, it) }
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
                commonNode(temp.last(), list.first()) != null -> {
                    val commonNode = checkNotNull(commonNode(temp.last(), list.first()))
                    temp.add(Snapped(commonNode.point, commonNode, commonNode))
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

    private fun commonNode(left: Snapped, right: Snapped): Node? {
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

    internal suspend fun run(list: List<MapItemEntity.PathEntity>): List<List<Snapped>> =
        list
            .map { path ->
                path.vertices
                    .let { snapToRoad(it) }
                    .let { fillCorners(it) }
//                    .map { it.map { a -> a.point } }
//                    .map { MapItemEntity.PathEntity(it) }
            }
            .flatten()
            .also { it.toVisited() }

    internal fun List<List<Snapped>>.toVisited() {
        map { continous ->
            continous.zipWithNext { prev, next ->
                val nodes = prev.getUniqueNodes() + next.getUniqueNodes()
                check(nodes.size == 2) { "there is no line between $prev -> $next" }
//                val diff =

            }
        }
    }

    private fun Snapped.getUniqueNodes(): Set<Node> =
        when (point) {
            near1.point -> setOf(near1)
            near2.point -> setOf(near2)
            else -> setOf(near1, near2)
        }

    private fun Snapped.isNode() =
        near1 == near2 || near1.point == point || near2.point == point

    private infix fun Snapped.onSameEdge(right: Snapped): Boolean =
        (this.near1 == right.near1 && this.near2 == right.near2) ||
                (this.near1 == right.near2 && this.near2 == right.near1)
}

internal fun <T> List<T>.filterWithPrev(condition: (T, T) -> Boolean): List<T> {
    var prev: T? = null
    return filter { next ->
        (prev?.let { condition(it, next) } ?: true)
            .also { prev = next }
    }
}