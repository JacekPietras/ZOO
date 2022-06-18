package com.jacekpietras.zoo.domain.feature.pathfinder

import java.util.*
import kotlin.math.exp
import kotlin.random.Random

class SimulatedAnnealing<T> : SalesmanProblemSolver<T> {

    override suspend fun run(
        request: List<T>,
        distanceCalculation: suspend (T, T) -> Double,
    ): List<T> = simulateAnnealing(
        request = request,
        distanceCalculation = distanceCalculation,
    ).second

    suspend fun simulateAnnealing(
        request: List<T>,
        distanceCalculation: suspend (T, T) -> Double,
    ): Pair<Double, List<T>> {
        var t = startingTemperature
        var i = 0
        var bestDistance = request.distance(distanceCalculation)
        var bestTravel = request

        var travel = ArrayList(request)

        while (t > 0.1 && i < numberOfIterations) {
            val travelVariation = travel.makeVariation()

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

        return bestDistance to bestTravel
    }

    private fun List<T>.makeVariation(): ArrayList<T> =
        ArrayList(this).also {
            val a = generateRandomIndex()
            var b = generateRandomIndex()
            while (a == b) {
                b = generateRandomIndex()
            }
            Collections.swap(it, a, b)
        }

    private fun List<T>.generateRandomIndex(): Int =
        Random.nextInt(size)

    private suspend fun List<T>.distance(distanceCalculation: suspend (T, T) -> Double): Double =
        zipWithNext { a, b -> distanceCalculation.invoke(a, b) }.sum()

    private companion object {

        const val startingTemperature = 10000.0
        const val numberOfIterations = 100000
        const val coolingRate = 0.99
    }
}