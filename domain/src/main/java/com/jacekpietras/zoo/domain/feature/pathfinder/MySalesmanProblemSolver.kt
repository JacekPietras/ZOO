package com.jacekpietras.zoo.domain.feature.pathfinder

import com.jacekpietras.core.PointD
import com.jacekpietras.core.haversine
import com.jacekpietras.zoo.domain.business.GraphAnalyzer
import com.jacekpietras.zoo.domain.feature.planner.model.Stage
import com.jacekpietras.zoo.domain.model.RegionId
import com.jacekpietras.zoo.domain.repository.MapRepository

internal class MySalesmanProblemSolver(
    private val graphAnalyzer: GraphAnalyzer,
    private val mapRepository: MapRepository,
) {

    private val cache: MutableList<CachedCalculation> = mutableListOf()
    private val tsp: SalesmanProblemSolver<Stage> = SimulatedAnnealing()

    suspend fun findShortPath(stages: List<Stage>): List<Pair<Stage, List<PointD>>> {
        val methodRunCache = mutableMapOf<Pair<PointD, PointD>, Calculation>()

        val result = tsp.run(
            request = stages,
            distanceCalculation = { a, b -> getCalculation(a, b, methodRunCache).distance },
        )

        val points = result
            .zipWithNext { prev, next ->
                prev to getCalculation(prev, next, methodRunCache).list
            }
        val tail = result.last() to emptyList<PointD>()
        return points + tail
    }

    suspend fun getDistance(prev: Stage, next: Stage): Double =
        getCalculation(prev, next).distance

    private suspend fun getCalculation(prev: Stage, next: Stage, methodRunCache: MutableMap<Pair<PointD, PointD>, Calculation>? = null): Calculation =
        if (prev is Stage.InRegion && next is Stage.InRegion) {
            cache.find { it.from == prev.regionId && it.to == next.regionId }
                ?: calculate(prev.regionId, next.regionId)
        } else {
            // fixme check if it works!
            val prevPoint = prev.getCenter()
            val nextPoint = next.getCenter()
            val found = methodRunCache?.get(prevPoint to nextPoint)
            if (found != null) {
                found
            } else {
                val path = graphAnalyzer.getShortestPath(
                    prevPoint,
                    nextPoint,
                    technicalAllowedAtStart = false,
                    technicalAllowedAtEnd = false,
                )
                Calculation(
                    distance = path.toLengthInMeters(),
                    list = path,
                ).also { methodRunCache?.put(prevPoint to nextPoint, it) }
            }
        }

    private suspend fun calculate(prev: RegionId, next: RegionId): Calculation {
        val prevPoint = prev.getCenter()
        val nextPoint = next.getCenter()
        val list = graphAnalyzer.getShortestPath(
            prevPoint,
            nextPoint,
            technicalAllowedAtStart = false,
            technicalAllowedAtEnd = false,
        )
        val distance = list.toLengthInMeters()
        val calculationAsc = CachedCalculation(
            from = prev,
            to = next,
            distance = distance,
            list = list.reversed(),
        )
        val calculationDesc = CachedCalculation(
            from = next,
            to = prev,
            distance = distance,
            list = list,
        )

        cache.add(calculationAsc)
        cache.add(calculationDesc)

        return calculationAsc
    }

    private suspend fun Stage.getCenter(): PointD =
        when (this) {
            is Stage.InRegion -> this.regionId.getCenter()
            is Stage.InUserPosition -> this.point
        }

    private suspend fun RegionId.getCenter(): PointD =
        mapRepository.getCurrentRegions().first { it.first.id == this }.second.findCenter()

    private fun List<PointD>.toLengthInMeters(): Double =
        zipWithNext().sumOf { (p1, p2) -> haversine(p1.x, p1.y, p2.x, p2.y) }

    private class CachedCalculation(
        val from: RegionId,
        val to: RegionId,
        distance: Double,
        list: List<PointD>,
    ) : Calculation(distance, list)

    private open class Calculation(
        val distance: Double,
        val list: List<PointD>,
    )
}