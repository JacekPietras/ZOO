package com.jacekpietras.zoo.domain.feature.planner.interactor

import com.jacekpietras.geometry.PointD
import com.jacekpietras.geometry.haversine
import com.jacekpietras.zoo.domain.feature.map.interactor.ObserveTerminalNodesUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import timber.log.Timber
import kotlin.math.abs
import kotlin.math.asin
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class ObserveCurrentPlanPathWithOptimizationUseCase(
    private val observeCurrentPlanWithOptimizationUseCase: ObserveCurrentPlanWithOptimizationUseCase,
    private val observeTerminalNodesUseCase: ObserveTerminalNodesUseCase,
) {

    fun run(): Flow<NavigationPath> =
        combine(
            observePath(),
            observeTerminalNodesUseCase.run()
        ) { (points, nodes) ->
            val turnPoints = getTurnPoints(points, nodes)

            NavigationPath(
                points = points,
                firstTurn = turnPoints,
                firstTurnArrow = makeArrow(turnPoints),
            )
        }


    private fun makeArrow(list: List<PointD>): List<PointD> {
        if (list.size < 2) return emptyList()

        val arrowTip = list[list.size - 1]
        val arrowBase = pointInDistance(
            begin = arrowTip,
            end = list[list.size - 2],
            distance = ARROW_M,
        )

        return version2(
            arrowTip,
            arrowBase,
        )
    }

    private fun bearing(
        a: PointD,
        b: PointD,
    ): Double {
        val ax = Math.toRadians(a.x)
        val ay = Math.toRadians(a.y)
        val bx = Math.toRadians(b.x)
        val by = Math.toRadians(b.y)

        val y = sin(by - ay) * cos(bx)
        val x = cos(ax) * sin(bx) -
                sin(ax) * cos(bx) * cos(by - ay)
        val t = atan2(y, x)
        return (t * 180 / Math.PI + 360) % 360
    }

    private fun version2(
        arrowTip: PointD,
        arrowBase: PointD,
    ): List<PointD> {
        val bearing = bearing(arrowTip, arrowBase)

        val earthRadius = 6378000.1
        val d = ARROW_M / earthRadius
        val p1 = perpendicular(arrowBase, d, bearing + 90)
        val p2 = perpendicular(arrowBase, d, bearing - 90)

        val longerTip = pointInDistance(
            begin = arrowBase,
            end = arrowTip,
            distance = ARROW_M * 2,
        )

        val a = bearing(arrowTip, arrowBase)
        val b = bearing(p2, p1)
        val c = abs(a - b).toInt()

        Timber.e("dupa bearing angle $c (${a.toInt()})")

        return listOf(
            longerTip,
            p1,
            p2,
        )
    }

    private fun perpendicular(arrowBase: PointD, d: Double, b: Double): PointD {
        val brng = Math.toRadians(b)
        val lat1 = Math.toRadians(arrowBase.x)
        val lon1 = Math.toRadians(arrowBase.y)

        var x = asin(sin(lat1) * cos(d) + cos(lat1) * sin(d) * cos(brng))
        var y = lon1 + atan2(sin(brng) * sin(d) * cos(lat1), cos(d) - sin(lat1) * sin(x))

        x = Math.toDegrees(x)
        y = Math.toDegrees(y)

        return PointD(x, y)
    }

    private fun version1(
        arrowTip: PointD,
        arrowBase: PointD,
    ): List<PointD> {
        val distPoint = 0.001

        val latDiff = arrowBase.x - arrowTip.x
        val longDiff = arrowBase.y - arrowTip.y
        val length = sqrt(latDiff * latDiff + longDiff * longDiff)
        val uLat = latDiff / length
        val uLong = longDiff / length

        val newLat1 = arrowBase.x + (distPoint / 2) * uLong
        val newLong1 = arrowBase.y - (distPoint / 2) * uLat

        val newLat2 = arrowBase.x - (distPoint / 2) * uLong
        val newLong2 = arrowBase.y + (distPoint / 2) * uLat

        val point1 = pointInDistance(
            begin = arrowBase,
            end = PointD(newLat1, newLong1),
            distance = ARROW_M,
        )
        val point2 = pointInDistance(
            begin = arrowBase,
            end = PointD(newLat2, newLong2),
            distance = ARROW_M,
        )
        val longerTip = pointInDistance(
            begin = arrowBase,
            end = arrowTip,
            distance = ARROW_M * 2,
        )

        val a = bearing(arrowTip, arrowBase)
        val b = bearing(point2, point1)
        val c = abs(a - b).toInt()

        Timber.e("dupa bearing angle $c")

        return listOf(
            longerTip,
            point1,
            point2,
        )
    }

    private fun getTurnPoints(
        points: List<PointD>,
        nodes: List<PointD>,
    ): List<PointD> {
        val indexOfFirstTurn = points.indexOfFirst { it in nodes }
        val turnPoints = if (indexOfFirstTurn >= 0) {
            getBehindOfTurn(indexOfFirstTurn, points) + getForwardOfTurn(indexOfFirstTurn, points).drop(1)
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

    private fun getBehindOfTurn(first: Int, points: List<PointD>): List<PointD> =
        getAroundTurn(points, range = first downTo 0).reversed()

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

        const val DISTANCE_M = 10
        const val ARROW_M = 4
    }
}
