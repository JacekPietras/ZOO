package com.jacekpietras.zoo.domain.feature.tsp.algorithms

import com.jacekpietras.zoo.domain.feature.tsp.TSPAlgorithm
import org.junit.jupiter.api.Assertions.assertEquals
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt
import kotlin.random.Random
import kotlin.time.Duration
import kotlin.time.measureTime

internal suspend fun doTspTest(
    algorithm: TSPAlgorithm<City>,
    seed: Long,
    numberOfCities: Int,
    bestExpected: Double = 0.0,
    times: Int = 1,
) {
    val initial = generateInitialTravel(
        seed = seed,
        numberOfCities = numberOfCities,
    )
    val initialDistance = initial.distance()

    val (results, durations) = (1..times)
        .map { doTspTest(initial, algorithm) }
        .unzip()

    val resultMin = results.min()
    val resultMax = results.max()
    val durationMin = durations.min()
    val durationMax = durations.max()

    if (resultMin < bestExpected) {
        println("New record: $resultMin")
    }
    println("${initialDistance.toInt()} / ${resultMin.toInt()}..${resultMax.toInt()} / ${bestExpected.toInt()} | in $durationMin..$durationMax")
}

private suspend fun doTspTest(
    initial: List<City>,
    algorithm: TSPAlgorithm<City>,
): Pair<Double, Duration> {
    var result: Double

    val duration = measureTime {
        val (distance, tour) = algorithm.run(
            points = initial,
            distanceCalculation = { a, b -> a.distanceToCity(b) },
        )

        assertEquals(initial.size, tour.size, "Incorrect number in result")
        assertEquals(initial.toSet(), tour.toSet(), "Different sets")

        result = distance
    }

    return result to duration
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

internal fun List<City>.distance(): Double =
    zipWithNext { a, b -> a.distanceToCity(b) }.sum()

internal data class City(
    val x: Int,
    val y: Int,
) {

    fun distanceToCity(city: City): Double =
        sqrt(
            abs(x - city.x).toDouble().pow(2.0) +
                    abs(y - city.y).toDouble().pow(2.0)
        )
}