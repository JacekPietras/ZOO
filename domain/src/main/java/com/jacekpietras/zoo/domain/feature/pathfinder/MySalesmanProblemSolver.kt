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
    private val tsp: SalesmanProblemSolver<RegionId> = SimulatedAnnealing()

    suspend fun findShortPath(stages: List<Stage>): List<Pair<Stage, List<PointD>>> {
        val regions = stages.map { it.regionId }
        val result = tsp.run(
            request = regions,
            distanceCalculation = { a, b -> getDistance(a, b) },
        )

        val points = result
            .zipWithNext { prev, next ->
                Stage(prev) to getCalculation(prev, next).list
            }
        val tail = Stage(result.last()) to emptyList<PointD>()
        return points + tail
    }

    suspend fun getDistance(prev: RegionId, next: RegionId): Double =
        getCalculation(prev, next).distance

    private suspend fun getCalculation(prev: RegionId, next: RegionId): CachedCalculation =
        cache.find { it.from == prev && it.to == next }
            ?: calculate(prev, next)

    private suspend fun calculate(prev: RegionId, next: RegionId): CachedCalculation {
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

    private suspend fun RegionId.getCenter(): PointD =
        mapRepository.getCurrentRegions().first { it.first.id == this }.second.findCenter()

    private fun List<PointD>.toLengthInMeters(): Double =
        zipWithNext().sumOf { (p1, p2) -> haversine(p1.x, p1.y, p2.x, p2.y) }

    private class CachedCalculation(
        val from: RegionId,
        val to: RegionId,
        val distance: Double,
        val list: List<PointD>,
    )
}