package com.jacekpietras.zoo.domain.feature.vrp.algorithms

import com.jacekpietras.zoo.domain.feature.vrp.VRPWithFixedStagesAlgorithm
import timber.log.Timber
import kotlin.random.Random

internal class NearestNeighborVRP<T : Any> : VRPWithFixedStagesAlgorithm<T> {

    override suspend fun run(
        points: List<T>,
        distanceCalculation: suspend (T, T) -> Double,
        immutablePositions: List<Int>?,
    ): List<T> {
        if (points.size <= 2) {
            Timber.d("Optimization cannot be done, not enough points ${points.size}")
            return points
        }
        if (points.size - (immutablePositions?.size ?: 0) <= 2) {
            Timber.d("Optimization cannot be done, not enough points ${points.size} (${immutablePositions?.size} is blocked)")
            return points
        }

        val notVisited = points.toMutableList()
        immutablePositions?.sortedDescending()?.forEach(notVisited::removeAt)

        val startingIndex = Random.nextInt(notVisited.size)
        var current = notVisited.removeAt(startingIndex)
        val result = mutableListOf(current)

        while (notVisited.isNotEmpty()) {
            val nearestIndex = findNearest(notVisited, current, distanceCalculation)
            current = notVisited.removeAt(nearestIndex)
            result.add(current)
        }

        immutablePositions?.sorted()?.forEach { i ->
            result.add(i, points[i])
        }

        return result
    }

    private suspend fun findNearest(
        points: List<T>,
        from: T,
        distanceCalculation: suspend (T, T) -> Double,
    ): Int {
        var minDistance = Double.MAX_VALUE
        var minIndex = 0
        points.forEachIndexed { i, to ->
            val currentDistance = distanceCalculation(from, to)
            if (currentDistance < minDistance) {
                minDistance = currentDistance
                minIndex = i
            }
        }

        return minIndex
    }

    override suspend fun run(points: List<T>, distanceCalculation: suspend (T, T) -> Double): List<T> =
        run(points, distanceCalculation, null)
}