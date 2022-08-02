package com.jacekpietras.zoo.domain.feature.tsp

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.util.*
import kotlin.math.exp
import kotlin.random.Random

internal class SimulatedAnnealing<T> : TravelingSalesmanProblemAlgorithm<T> {

    override suspend fun run(
        points: List<T>,
        distanceCalculation: suspend (T, T) -> Double,
        immutablePositions: List<Int>?,
    ): Pair<Double, List<T>> {
        if (points.size <= 2) {
            Timber.d("Optimization cannot be done, not enough points ${points.size}")
            return points.distance(distanceCalculation) to points
        }
        if (points.size - (immutablePositions?.size ?: 0) <= 2) {
            Timber.d("Optimization cannot be done, not enough points ${points.size} (${immutablePositions?.size} is blocked)")
            return points.distance(distanceCalculation) to points
        }
        return withContext(Dispatchers.Default) {
            var t = startingTemperature
            var i = 0
            var bestDistance = points.distance(distanceCalculation)
            var bestTravel = points

            var travel = ArrayList(points)

            while (t > 0.1 && i < numberOfIterations && isActive) {
                val travelVariation = travel.makeVariation(immutablePositions)

                val variationDistance = travelVariation.distance(distanceCalculation)
                if (variationDistance < bestDistance) {
                    bestDistance = variationDistance
                    bestTravel = travelVariation
                } else if (exp((bestDistance - variationDistance) / t) < Math.random()) {
                    travel = travelVariation
                }

                t *= coolingRate
                i++
            }

            bestDistance to bestTravel
        }
    }

    private fun List<T>.makeVariation(
        immutablePositions: List<Int>? = null,
    ): ArrayList<T> {
        val minimalSizeToVariate = (immutablePositions?.size ?: 0) + 2
        if (size < minimalSizeToVariate) throw IllegalStateException("Size $size is too small to make variation with ${immutablePositions?.size} blocked positions")
        return ArrayList(this).also {
            val a = generateRandomIndex(immutablePositions)
            val b = generateRandomIndex((immutablePositions ?: emptyList()) + a)
            Collections.swap(it, a, b)
        }
    }

    private fun List<T>.generateRandomIndex(ignored: List<Int>?): Int {
        while (true) {
            val number = Random.nextInt(size)
            if (ignored?.contains(number) != true) {
                return number
            }
        }
    }

    private suspend fun List<T>.distance(distanceCalculation: suspend (T, T) -> Double): Double =
        zipWithNext { a, b -> distanceCalculation.invoke(a, b) }.sum()

    private companion object {

        const val startingTemperature = 10000.0
        const val numberOfIterations = 100000
        const val coolingRate = 0.99
    }
}