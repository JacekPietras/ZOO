package com.jacekpietras.zoo.domain.feature.pathfinder

import com.jacekpietras.core.PointD
import com.jacekpietras.core.haversine
import com.jacekpietras.zoo.domain.business.GraphAnalyzer
import com.jacekpietras.zoo.domain.model.RegionId
import com.jacekpietras.zoo.domain.repository.MapRepository

internal class MySalesmanProblemSolver(
    private val graphAnalyzer: GraphAnalyzer,
    private val mapRepository: MapRepository,
) {

    private val cache: MutableList<CachedCalculation> = mutableListOf()
    private val tsp: SalesmanProblemSolver<RegionId> = SimulatedAnnealing()

    suspend fun findShortPath(regions: List<RegionId>): List<Pair<RegionId, List<PointD>>> {
        val result = tsp.run(
            request = regions,
            distanceCalculation = { a, b -> getCalculation(a, b).distance },
        )

        val points = result
            .zipWithNext { prev, next ->
                prev to getCalculation(prev, next).list
            }
        val tail = result.last() to emptyList<PointD>()
        return points + tail
    }

    private suspend fun getCalculation(prev: RegionId, next: RegionId): CachedCalculation =
        cache.find { it.from == prev && it.to == next }
            ?: calculate(prev, next)

    private suspend fun calculate(prev: RegionId, next: RegionId): CachedCalculation {
        val prevPoint = prev.getCenter()
        val nextPoint = next.getCenter()
        val list = graphAnalyzer.getShortestPath(prevPoint, nextPoint)
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