package com.jacekpietras.zoo.domain.feature.tsp.algorithms

import com.jacekpietras.zoo.domain.feature.tsp.TravelingSalesmanProblemAlgorithm
import kotlinx.coroutines.test.runTest
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt
import kotlin.random.Random
import kotlin.time.measureTime

internal fun doTspTest(algorithm: TravelingSalesmanProblemAlgorithm<City>, seed: Long, numberOfCities: Int, bestExpected: Double = 0.0) = runTest {
    val initial = generateInitialTravel(
        seed = seed,
        numberOfCities = numberOfCities,
    )
    val initialDistance = initial.distance()
    var result: Double

    val duration = measureTime {
        result = algorithm.run(
            points = initial,
            distanceCalculation = { a, b -> a.distanceToCity(b) },
        ).first
    }

    if (result < bestExpected) {
        println("New record: $result")
    }

    println("${initialDistance.toInt()} / ${result.toInt()} / ${bestExpected.toInt()} | in $duration")
}

private fun generateInitialTravel(
    seed: Long,
    numberOfCities: Int,
): List<City> {
    val random = Random(seed)
    return MutableList(numberOfCities) {
        City(
            x = (random.nextDouble() * 500).toInt(),
            y = (random.nextDouble() * 500).toInt(),
        )
    }
}

private fun List<City>.distance(): Double =
    zipWithNext { a, b -> a.distanceToCity(b) }.sum()

internal class City(
    var x: Int,
    var y: Int,
) {

    fun distanceToCity(city: City): Double =
        sqrt(
            abs(x - city.x).toDouble().pow(2.0) +
                    abs(y - city.y).toDouble().pow(2.0)
        )
}