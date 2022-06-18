package com.jacekpietras.zoo.domain.feature.pathfinder

import java.util.*
import kotlin.math.exp
import kotlin.random.Random

class SimulatedAnnealing<Obj> {

    fun simulateAnnealing(
        request: List<Obj>,
        distanceCalculation: (Obj, Obj) -> Double,
        startingTemperature: Double = 10000000.0,
        numberOfIterations: Int = 100000,
        coolingRate: Double = 0.999,
    ): Pair<Double, List<Obj>> {

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

    private fun <T> List<T>.makeVariation(): ArrayList<T> {
        val a = generateRandomIndex()
        var b = generateRandomIndex()
        while (a == b) {
            b = generateRandomIndex()
        }

        val variation = ArrayList(this)
        Collections.swap(variation, a, b)

        return variation
    }

    private fun <T> List<T>.generateRandomIndex(): Int =
        Random.nextInt(size)

    private fun List<Obj>.distance(distanceCalculation: (Obj, Obj) -> Double): Double =
        zipWithNext { a, b -> distanceCalculation(a, b) }.sum()
}