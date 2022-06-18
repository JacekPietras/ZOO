package com.jacekpietras.zoo.domain.feature.pathfinder

import kotlin.math.exp

class SimulatedAnnealing {

    fun <Obj> simulateAnnealing(
        request: List<Obj>,
        distanceCalculation: (Obj, Obj) -> Double,
        startingTemperature: Double = 100000.0,
        numberOfIterations: Int = 100000,
        coolingRate: Double = 0.9,
    ): Pair<Double, List<Obj>> {
        val travel = Travel(travel = ArrayList(request))
        var t = startingTemperature
        var bestDistance = travel.distance(distanceCalculation)

        for (i in 0 until numberOfIterations) {
            if (t > 0.1) {
                travel.swapRandomly()
                val currentDistance = travel.distance(distanceCalculation)
                if (currentDistance < bestDistance) {
                    bestDistance = currentDistance
                } else if (exp((bestDistance - currentDistance) / t) < Math.random()) {
                    travel.revertSwap()
                }
                t *= coolingRate
            } else {
                break
            }
        }
        return bestDistance to travel.travel
    }

    private fun <Obj> Travel<Obj>.distance(distanceCalculation: (Obj, Obj) -> Double): Double =
        travel.zipWithNext { a, b -> distanceCalculation(a, b) }.sum()
}