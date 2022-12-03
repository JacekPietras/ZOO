package com.jacekpietras.zoo.domain.feature.tsp

import com.jacekpietras.geometry.PointD
import com.jacekpietras.geometry.haversine
import com.jacekpietras.zoo.domain.feature.map.repository.MapRepository
import com.jacekpietras.zoo.domain.feature.pathfinder.GraphAnalyzer
import com.jacekpietras.zoo.domain.feature.planner.model.Stage
import com.jacekpietras.zoo.domain.model.RegionId
import timber.log.Timber

internal class StageTravellingSalesmanProblemSolverImpl(
    private val graphAnalyzer: GraphAnalyzer,
    private val mapRepository: MapRepository,
    private val tspAlgorithm: TravelingSalesmanProblemAlgorithm<Stage>,
) : StageTravellingSalesmanProblemSolver {

    private var regionCalculationCache = emptyList<RegionCalculation>()
    private val optionCreator = StageListOptionCreator()

    override suspend fun findShortPathAndStages(
        stages: List<Stage>,
    ): TspResult {
        val pointCalculationCache = PointCalculationCache()

        val resultStages = findShortStages(stages, pointCalculationCache)
        val pathParts = resultStages.makePath(pointCalculationCache)
        val stops = pathParts.map { it.last() }

        return TspResult(resultStages, stops, pathParts.flatten())
    }

    private suspend fun findShortStages(
        stages: List<Stage>,
        pointCalculationCache: PointCalculationCache,
    ): List<Stage> {
        val immutablePositions = stages.immutablePositions()
        var minDistance = Double.MAX_VALUE
        var resultStages = stages

        optionCreator.run(
            toCheck = stages,
            onOptionFound = { stageOption ->
                val (distance, newStages) = tspAlgorithm.run(
                    points = stageOption,
                    distanceCalculation = { a, b -> calculate(a, b, pointCalculationCache).distance },
                    immutablePositions = immutablePositions,
                )
                if (minDistance > distance) {
                    minDistance = distance
                    resultStages = newStages
                }
            }
        )

        Timber.d("Optimization record ${minDistance.toInt()}m")

        return resultStages
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

    override suspend fun getDistance(prev: Stage, next: Stage): Double =
        calculate(prev, next, PointCalculationCache()).distance

    private suspend fun calculate(prev: Stage, next: Stage, pointCalculationCache: PointCalculationCache): Calculation =
        if (prev is Stage.InRegion && next is Stage.InRegion) {
            findRegionCalculation(makeKey(prev.region.id, next.region.id))
                ?: calculateRegion(prev.region.id, next.region.id)
        } else {
            val prevPoint = prev.getCenter()
            val nextPoint = next.getCenter()
            pointCalculationCache[prevPoint to nextPoint]
                ?: calculatePoint(prevPoint, nextPoint, pointCalculationCache)
        }

    private fun findRegionCalculation(key: String) =
        regionCalculationCache.binarySearchBy(key, selector = RegionCalculation::key)
            .let(regionCalculationCache::getOrNull)

    private fun makeKey(prev: RegionId, next: RegionId): String =
        prev.id + "?" + next.id

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
            key = makeKey(prev, next),
            distance = distance,
            path = list.reversed(),
        )
        val calculationDesc = RegionCalculation(
            key = makeKey(next, prev),
            distance = distance,
            path = list,
        )

        regionCalculationCache = (regionCalculationCache + calculationAsc + calculationDesc)
            .sortedBy(RegionCalculation::key)

        return calculationAsc
    }

    private suspend fun calculatePoint(
        prevPoint: PointD,
        nextPoint: PointD,
        pointCalculationCache: PointCalculationCache
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
        ).also { pointCalculationCache[prevPoint to nextPoint] = it }
    }

    private suspend fun Stage.getCenter(): PointD =
        when (this) {
            is Stage.InRegion -> this.region.id.getCenter()
            is Stage.InUserPosition -> this.point
        }

    private suspend fun RegionId.getCenter(): PointD =
        mapRepository.getCurrentRegions().firstOrNull { it.first.id == this }?.second?.findCenter()
            ?: throw IllegalStateException("No region with id $this")

    private fun List<PointD>.toLengthInMeters(): Double =
        zipWithNext().sumOf { (p1, p2) -> haversine(p1.x, p1.y, p2.x, p2.y) }
}

private typealias PointCalculationCache = LinkedHashMap<Pair<PointD, PointD>, Calculation>

private class RegionCalculation(
    val key: String,
    distance: Double,
    path: List<PointD>,
) : Calculation(distance, path)

private open class Calculation(
    val distance: Double,
    val path: List<PointD>,
)