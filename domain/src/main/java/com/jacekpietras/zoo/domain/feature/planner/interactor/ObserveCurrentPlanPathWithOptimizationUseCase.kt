package com.jacekpietras.zoo.domain.feature.planner.interactor

import com.jacekpietras.geometry.PointD
import com.jacekpietras.geometry.haversine
import com.jacekpietras.zoo.domain.feature.map.interactor.ObserveTerminalNodesUseCase
import com.jacekpietras.zoo.domain.feature.pathfinder.interactor.GetShortestPathFromUserUseCase
import com.jacekpietras.zoo.domain.feature.planner.model.Stage
import com.jacekpietras.zoo.domain.interactor.GetRegionCenterPointUseCase
import com.jacekpietras.zoo.domain.model.RegionId
import com.jacekpietras.zoo.domain.utils.toLengthInMeters
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onStart
import kotlin.math.asin
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

class ObserveCurrentPlanPathWithOptimizationUseCase(
    private val observeCurrentPlanWithOptimizationUseCase: ObserveCurrentPlanWithOptimizationUseCase,
    private val observeTerminalNodesUseCase: ObserveTerminalNodesUseCase,
    private val getRegionCenterPointUseCase: GetRegionCenterPointUseCase,
    private val getShortestPathUseCase: GetShortestPathFromUserUseCase,
) {

    fun run(): Flow<NavigationPlan> =
        combine(
            observeCurrentPlanWithOptimizationUseCase.run(),
            observeTerminalNodesUseCase.run()
        ) { result, nodes ->
            var plan = NavigationPlan(
                points = result.path,
                stops = result.stops,
                stages = result.stages,
            )

            if (result.stages.any { it is Stage.InUserPosition }) {
                plan = plan.withNextDestination()

                val turnWithArrow = getIndexOfTurnWithArrow(result.path, nodes)
                if (turnWithArrow != null) {
                    plan = plan.withArrow(turnWithArrow)
                }
            }

            plan
        }
            .onStart { emit(NavigationPlan()) }

    private suspend fun NavigationPlan.withNextDestination(): NavigationPlan {
        val firstDestinationStage = stages
            .filterIsInstance<Stage.InRegion>()
            .firstOrNull { !it.seen }
        val distanceToFirstDestinationStage = firstDestinationStage?.let { stage ->
            val centerPoint = getRegionCenterPointUseCase.run(regionId = stage.region.id)
            val path = getShortestPathUseCase.run(centerPoint)
            path.takeIf { it.size > 1 }?.toLengthInMeters()
        }
        return copy(
            nextStageRegion = firstDestinationStage?.region?.id,
            distanceToNextStage = distanceToFirstDestinationStage,
        )
    }

    private fun NavigationPlan.withArrow(
        turnWithArrow: Int,
    ): NavigationPlan {
        val turnPoints = getTurnPoints(turnWithArrow, points)
        val arrowInnerPoints = makeArrow(turnPoints, ARROW_INNER_M)
        val arrowOuterPoints = makeSharpBorder(arrowInnerPoints, ARROW_OUTER_M)
        val turnInnerPoints = makeLineBorder(turnPoints, ARROW_TAIL_INNER_M)
        val turnOuterPoints = makeLineBorder(turnPoints.moveFirstForward(ARROW_OUTER_M), ARROW_TAIL_INNER_M + ARROW_OUTER_M)

        return copy(
            firstTurn = turnPoints,
            firstTurnArrowInner = listOf(arrowInnerPoints, turnInnerPoints),
            firstTurnArrowOuter = listOf(arrowOuterPoints, turnOuterPoints),
        )
    }

    private fun List<PointD>.moveFirstForward(distance: Int): List<PointD> =
        listOf(pointInDistance(this[0], this[1], -distance)) + drop(1)

    private fun makeSharpBorder(points: List<PointD>, size: Int): List<PointD> {
        val edges = (points + points[0]).zipWithNext()
        val edgesWithBearing = edges.map { (a, b) ->
            Triple(a, b, bearing(a, b) + 90)
        }
        return edgesWithBearing.map { (a, b, bearing) ->
            listOf(
                perpendicular(perpendicular(a, size, bearing), size, bearing + 90),
                perpendicular(perpendicular(b, size, bearing), size, bearing - 90),
            )
        }.flatten()
    }

    private fun makeLineBorder(points: List<PointD>, size: Int): List<PointD> {
        val edges = points.zipWithNext() + points.reversed().zipWithNext()
        val edgesWithBearing = edges.map { (a, b) ->
            Triple(a, b, bearing(a, b) + 90)
        }
        return edgesWithBearing.map { (a, b, bearing) ->
            listOf(
                perpendicular(a, size, bearing),
                perpendicular(b, size, bearing),
            )
        }.flatten()
    }

    private fun getIndexOfTurnWithArrow(
        points: List<PointD>,
        nodes: List<PointD>
    ): Int? {
        points
            .indexOfFirstTurns(nodes)
            .forEach { turnPointIndex ->
                val turnPoint = points[turnPointIndex]
                val countOfCrossingsOnTurn = points.count { it == turnPoint }
                val arrowIsNeeded = countOfCrossingsOnTurn > 1
                if (arrowIsNeeded) {
                    return turnPointIndex
                }
            }
        return null
    }

    private fun List<PointD>.indexOfFirstTurns(nodes: List<PointD>): List<Int> =
        indexOfFirst(predicate = { it in nodes }, amount = 2)

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

    private fun makeArrow(list: List<PointD>, size: Int): List<PointD> {
        if (list.size < 2) return emptyList()

        val arrowTip = list[list.size - 1]
        val arrowBase = pointInDistance(
            begin = arrowTip,
            end = list[list.size - 2],
            distance = size,
        )

        return makeArrowHeadPath(
            arrowTip,
            arrowBase,
            size,
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
        size: Int
    ): List<PointD> {
        val bearing = bearing(arrowTip, arrowBase)

        val p1 = perpendicular(arrowBase, size, bearing + 90)
        val p2 = perpendicular(arrowBase, size, bearing - 90)

        val longerTip = pointInDistance(
            begin = arrowBase,
            end = arrowTip,
            distance = size * 2,
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

    data class NavigationPlan(
        val points: List<PointD> = emptyList(),
        val stops: List<PointD> = emptyList(),
        val firstTurn: List<PointD> = emptyList(),
        val firstTurnArrowInner: List<List<PointD>> = emptyList(),
        val firstTurnArrowOuter: List<List<PointD>> = emptyList(),
        val stages: List<Stage> = emptyList(),
        val distanceToNextStage: Double? = null,
        val nextStageRegion: RegionId? = null,
    )

    private companion object {

        const val EARTH_RADIUS = 6378000.1
        const val MAGIC = 1.6
        const val DISTANCE_M = 10
        const val ARROW_INNER_M = 4
        const val ARROW_TAIL_INNER_M = 1
        const val ARROW_OUTER_M = 1
    }
}
