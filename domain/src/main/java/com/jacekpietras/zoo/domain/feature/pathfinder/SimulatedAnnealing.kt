package com.jacekpietras.zoo.domain.feature.pathfinder

import java.util.*
import kotlin.math.exp
import kotlin.random.Random

class SimulatedAnnealing<T> {

    fun simulateAnnealing(
        request: List<T>,
        distanceCalculation: (T, T) -> Double,
        startingTemperature: Double = 10000.0,
        numberOfIterations: Int = 100000,
        coolingRate: Double = 0.99,
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

    private fun List<T>.distance(distanceCalculation: (T, T) -> Double): Double =
        zipWithNext { a, b -> distanceCalculation(a, b) }.sum()
}