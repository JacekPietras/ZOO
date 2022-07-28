package com.jacekpietras.zoo.domain.feature.planner.interactor

import com.jacekpietras.geometry.PointD
import com.jacekpietras.geometry.haversine
import com.jacekpietras.zoo.domain.feature.map.interactor.ObserveTerminalNodesUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart

class ObserveCurrentPlanPathWithOptimizationUseCase(
    private val observeCurrentPlanWithOptimizationUseCase: ObserveCurrentPlanWithOptimizationUseCase,
    private val observeTerminalNodesUseCase: ObserveTerminalNodesUseCase,
) {

    fun run(): Flow<NavigationPath> =
        combine(
            observePath(),
            observeTerminalNodesUseCase.run(),
        ) { (points, nodes) ->
            NavigationPath(
                points = points,
                firstTurn = getTurnPoints(points, nodes),
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
    )

    private companion object {

        const val DISTANCE_M = 10
    }
}
