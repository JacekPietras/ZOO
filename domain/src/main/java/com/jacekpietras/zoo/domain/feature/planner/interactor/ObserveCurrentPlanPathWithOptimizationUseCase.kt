package com.jacekpietras.zoo.domain.feature.planner.interactor

import com.jacekpietras.geometry.PointD
import com.jacekpietras.geometry.haversine
import com.jacekpietras.zoo.domain.feature.map.interactor.ObserveTerminalNodesUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlin.math.asin
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

class ObserveCurrentPlanPathWithOptimizationUseCase(
    private val observeCurrentPlanWithOptimizationUseCase: ObserveCurrentPlanWithOptimizationUseCase,
    private val observeTerminalNodesUseCase: ObserveTerminalNodesUseCase,
) {

    fun run(): Flow<NavigationPath> =
        combine(
            observePath(),
            observeTerminalNodesUseCase.run()
        ) { (points, nodes) ->

            val indexOfFirstTurns = points.indexOfFirst(predicate = { it in nodes }, amount = 2)
            val turnWithArrow = indexOfFirstTurns
                .mapNotNull { turnPointIndex ->
                    val turnPoint = points[turnPointIndex]
                    val countOfCrossingsOnTurn = points.count { it == turnPoint }
                    val arrowIsNeeded = countOfCrossingsOnTurn > 1
                    if (arrowIsNeeded) {
                        turnPointIndex
                    } else {
                        null
                    }
                }
                .firstOrNull()

            if (turnWithArrow != null) {
                val turnPoints = getTurnPoints(turnWithArrow, points)

                NavigationPath(
                    points = points,
                    firstTurn = turnPoints,
                    firstTurnArrow = makeArrow(turnPoints),
                )
            } else {
                NavigationPath(
                    points = points,
                    firstTurn = emptyList(),
                    firstTurnArrow = emptyList(),
                )
            }
        }

    private inline fun <T> List<T>.indexOfFirst(predicate: (T) -> Boolean, amount: Int): List<Int> {
        val result = mutableListOf<Int>()
        for ((index, item) in this.withIndex()) {
            if (predicate(item)) {
                result.add(index)
                if (result.size == amount) return result
            }
        }
        return result
    }

    private fun makeArrow(list: List<PointD>): List<PointD> {
        if (list.size < 2) return emptyList()

        val arrowTip = list[list.size - 1]
        val arrowBase = pointInDistance(
            begin = arrowTip,
            end = list[list.size - 2],
            distance = ARROW_M,
        )

        return makeArrowHeadPath(
            arrowTip,
            arrowBase,
        )
    }

    private fun bearing(
        a: PointD,
        b: PointD,
    ): Double {
        val ax = Math.toRadians(a.x / MAGIC)
        val ay = Math.toRadians(a.y)
        val bx = Math.toRadians(b.x / MAGIC)
        val by = Math.toRadians(b.y)

        val y = sin(by - ay) * cos(bx)
        val x = cos(ax) * sin(bx) -
                sin(ax) * cos(bx) * cos(by - ay)
        val t = atan2(y, x)
        return (t * 180 / Math.PI + 360) % 360
    }

    private fun makeArrowHeadPath(
        arrowTip: PointD,
        arrowBase: PointD,
    ): List<PointD> {
        val bearing = bearing(arrowTip, arrowBase)

        val p1 = perpendicular(arrowBase, ARROW_M, bearing + 90)
        val p2 = perpendicular(arrowBase, ARROW_M, bearing - 90)

        val longerTip = pointInDistance(
            begin = arrowBase,
            end = arrowTip,
            distance = ARROW_M * 2,
        )

        return listOf(
            longerTip,
            p1,
            p2,
        )
    }

    private fun perpendicular(center: PointD, distance: Number, b: Double): PointD {
        val d = distance.toDouble() / EARTH_RADIUS
        val brng = Math.toRadians(b)
        val lat1 = Math.toRadians(center.x / MAGIC)
        val lon1 = Math.toRadians(center.y)

        var x = asin(sin(lat1) * cos(d) + cos(lat1) * sin(d) * cos(brng))
        var y = lon1 + atan2(sin(brng) * sin(d) * cos(lat1), cos(d) - sin(lat1) * sin(x))

        x = Math.toDegrees(x) * MAGIC
        y = Math.toDegrees(y)

        return pointInDistance(
            begin = center,
            end = PointD(x, y),
            distance = distance,
        )
    }

    private fun getTurnPoints(
        indexOfTurn: Int,
        points: List<PointD>,
    ): List<PointD> {
        val turnPoints = if (indexOfTurn >= 0) {
            getBehindOfTurn(indexOfTurn, points) + getForwardOfTurn(indexOfTurn, points).drop(1)
        } else {
            emptyList()
        }
        return turnPoints
    }

    private fun getAroundTurn(points: List<PointD>, range: IntProgression): List<PointD> {
        var prev = points[range.first]
        val listOfPoints = mutableListOf<PointD>()
        var distance = 0.0

        for (i in range) {
            val next = points[i]
            val distanceToNext = haversine(prev.x, prev.y, next.x, next.y)
            if (distance + distanceToNext < DISTANCE_M) {
                distance += distanceToNext
                listOfPoints += next
                prev = next
            } else {
                val distanceLeft = DISTANCE_M - distance
                return listOfPoints + pointInPercent(
                    begin = prev,
                    end = next,
                    percent = distanceLeft / distanceToNext,
                )
            }
        }
        return listOfPoints
    }

    private fun getBehindOfTurn(last: Int, points: List<PointD>): List<PointD> =
        getAroundTurn(points, range = last downTo 0).reversed()

    private fun getForwardOfTurn(first: Int, points: List<PointD>): List<PointD> =
        getAroundTurn(points, range = first until points.size)

    private fun pointInDistance(begin: PointD, end: PointD, distance: Number): PointD {
        val pointDistance = haversine(begin.x, begin.y, end.x, end.y)
        return pointInPercent(
            begin = begin,
            end = end,
            percent = distance.toDouble() / pointDistance,
        )
    }

    private fun pointInPercent(begin: PointD, end: PointD, percent: Double): PointD {
        val diff = end - begin
        return begin + (diff * percent)
    }

    private fun observePath() =
        observeCurrentPlanWithOptimizationUseCase.run()
            .map { (_, path) -> path }
            .onStart { emit(emptyList()) }

    class NavigationPath(
        val points: List<PointD>,
        val firstTurn: List<PointD>,
        val firstTurnArrow: List<PointD>,
    )

    private companion object {

        const val EARTH_RADIUS = 6378000.1
        const val MAGIC = 1.6
        const val DISTANCE_M = 10
        const val ARROW_M = 4
    }
}
