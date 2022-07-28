package com.jacekpietras.zoo.domain.feature.planner.interactor

import com.jacekpietras.geometry.PointD
import com.jacekpietras.geometry.haversine
import com.jacekpietras.zoo.domain.feature.map.interactor.ObserveTerminalNodesUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlin.math.sqrt

class ObserveCurrentPlanPathWithOptimizationUseCase(
    private val observeCurrentPlanWithOptimizationUseCase: ObserveCurrentPlanWithOptimizationUseCase,
    private val observeTerminalNodesUseCase: ObserveTerminalNodesUseCase,
) {

    fun run(): Flow<NavigationPath> =
        combine(
            observePath(),
            observeTerminalNodesUseCase.run(),
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

        return version1(
            arrowTip,
            arrowBase,
        )
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
        const val ARROW_M = 5
    }
}
