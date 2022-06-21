package com.jacekpietras.zoo.domain.feature.tsp

import com.jacekpietras.core.PointD
import com.jacekpietras.core.haversine
import com.jacekpietras.zoo.domain.feature.map.repository.MapRepository
import com.jacekpietras.zoo.domain.feature.pathfinder.GraphAnalyzer
import com.jacekpietras.zoo.domain.feature.planner.model.Stage
import com.jacekpietras.zoo.domain.model.RegionId

internal class StageTravellingSalesmanProblemSolver(
    private val graphAnalyzer: GraphAnalyzer,
    private val mapRepository: MapRepository,
    private val tspAlgorithm: TravelingSalesmanProblemAlgorithm<Stage>,
) {

    private val cache: MutableList<RegionCalculation> = mutableListOf()
    private val optionCreator = StageListOptionCreator()

    suspend fun findShortPath(
        stages: List<Stage>,
    ): Pair<List<Stage>, List<PointD>> {
        val immutablePositions = stages.immutablePositions()
        val pointCalculationCache = PointCalculationCache()

        val (_, resultStages) = optionCreator.run(stages)
            .map { stageOption ->
                tspAlgorithm.run(
                    points = stageOption,
                    distanceCalculation = { a, b -> calculate(a, b, pointCalculationCache).distance },
                    immutablePositions = immutablePositions,
                )
            }
            .minByOrNull { (distance, _) -> distance }!!

        return Pair(resultStages, resultStages.makePath(pointCalculationCache))
    }

    private fun List<Stage>.immutablePositions() =
        mapIndexed { i, stage ->
            when (stage) {
                is Stage.InRegion -> i.takeIf { !stage.mutable }
                is Stage.InUserPosition -> i
            }
        }.filterNotNull()

    private suspend fun List<Stage>.makePath(pointCalculationCache: PointCalculationCache) =
        zipWithNext { prev, next -> calculate(prev, next, pointCalculationCache).path }
            .flatten()

    suspend fun getDistance(prev: Stage, next: Stage): Double =
        calculate(prev, next, PointCalculationCache()).distance

    private suspend fun calculate(prev: Stage, next: Stage, pointCalculationCache: PointCalculationCache): Calculation =
        if (prev is Stage.InRegion && next is Stage.InRegion) {
            findRegionCalculation(prev, next)
                ?: calculateRegion(prev.region.id, next.region.id)
        } else {
            val prevPoint = prev.getCenter()
            val nextPoint = next.getCenter()
            pointCalculationCache[prevPoint to nextPoint]
                ?: calculatePoint(prevPoint, nextPoint, pointCalculationCache)
        }

    private fun findRegionCalculation(prev: Stage.InRegion, next: Stage.InRegion) =
        cache.find { it.from == prev.region.id && it.to == next.region.id }

    private suspend fun calculateRegion(prev: RegionId, next: RegionId): Calculation {
        val prevPoint = prev.getCenter()
        val nextPoint = next.getCenter()
        val list = graphAnalyzer.getShortestPath(
            prevPoint,
            nextPoint,
            technicalAllowedAtStart = false,
            technicalAllowedAtEnd = false,
        )
        val distance = list.toLengthInMeters()
        val calculationAsc = RegionCalculation(
            from = prev,
            to = next,
            distance = distance,
            path = list.reversed(),
        )
        val calculationDesc = RegionCalculation(
            from = next,
            to = prev,
            distance = distance,
            path = list,
        )

        cache.add(calculationAsc)
        cache.add(calculationDesc)

        return calculationAsc
    }

    private suspend fun calculatePoint(
        prevPoint: PointD,
        nextPoint: PointD,
        pointCalculationCache: PointCalculationCache?
    ): Calculation {
        val path = graphAnalyzer.getShortestPath(
            prevPoint,
            nextPoint,
            technicalAllowedAtStart = false,
            technicalAllowedAtEnd = false,
        ).reversed()
        return Calculation(
            distance = path.toLengthInMeters(),
            path = path,
        ).also { pointCalculationCache?.put(prevPoint to nextPoint, it) }
    }

    private suspend fun Stage.getCenter(): PointD =
        when (this) {
            is Stage.InRegion -> this.region.id.getCenter()
            is Stage.InUserPosition -> this.point
        }

    private suspend fun RegionId.getCenter(): PointD =
        mapRepository.getCurrentRegions().first { it.first.id == this }.second.findCenter()

    private fun List<PointD>.toLengthInMeters(): Double =
        zipWithNext().sumOf { (p1, p2) -> haversine(p1.x, p1.y, p2.x, p2.y) }
}

private typealias PointCalculationCache = LinkedHashMap<Pair<PointD, PointD>, Calculation>

private class RegionCalculation(
    val from: RegionId,
    val to: RegionId,
    distance: Double,
    path: List<PointD>,
) : Calculation(distance, path)

private open class Calculation(
    val distance: Double,
    val path: List<PointD>,
)