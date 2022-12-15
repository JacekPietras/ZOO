package com.jacekpietras.zoo.domain.feature.tsp.algorithms

import com.jacekpietras.zoo.domain.feature.tsp.TSPWithFixedStagesAlgorithm
import org.junit.jupiter.api.Assertions.assertEquals
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt
import kotlin.random.Random
import kotlin.time.Duration
import kotlin.time.measureTime

internal suspend fun doTspTest(
    algorithm: TSPWithFixedStagesAlgorithm<City>,
    seed: Long,
    numberOfCities: Int,
    bestExpected: Double = 0.0,
    times: Int = 1,
    immutablePositions: List<Int>? = null,
) {
    val initial = generateInitialTravel(
        seed = seed,
        numberOfCities = numberOfCities,
    )
    val initialDistance = initial.distance().toInt()
    try {
        val (results, durations) = (1..times)
            .map { doTspTest(initial, algorithm, immutablePositions) }
            .unzip()

        val resultMin = results.min().toInt()
        val resultMax = results.max().toInt()
        val errorMin = ((resultMin - bestExpected) / (initialDistance - bestExpected.toInt()) * 100).toInt()
        val errorMax = ((resultMax - bestExpected) / (initialDistance - bestExpected.toInt()) * 100).toInt()
        val durationAvg = durations.map { it.inWholeMicroseconds }.sorted().dropBorder().average().toInt()

        if (results.min() < bestExpected) {
            println("New record: ${results.min()}")
        }
        val errorString = if (errorMin != errorMax) {
            "$errorMin..$errorMax%"
        } else {
            "$errorMin%"
        }.padStart(7)
        println("err: $errorString in $durationAvg μs")

    } catch (e: Exception) {
        println("crashed: " + e.message)
    }
}

private fun <T> List<T>.dropBorder(): List<T> =
    if (size > 2) {
        drop(1).dropLast(1)
    } else {
        this
    }

private suspend fun doTspTest(
    initial: List<City>,
    algorithm: TSPWithFixedStagesAlgorithm<City>,
    immutablePositions: List<Int>? = null,
): Pair<Double, Duration> {
    var tour: List<City>
    val fixedPointsBefore = immutablePositions?.map { initial[it] }

    val duration = measureTime {
        tour = algorithm.run(
            points = initial,
            distanceCalculation = { a, b -> a.distanceToCity(b) },
            immutablePositions = immutablePositions,
        )
    }

    val fixedPointsAfter = immutablePositions?.map { tour[it] }
    val distance = tour.zipWithNext { a, b -> a.distanceToCity(b) }.sum()

    assertEquals(initial.size, tour.size, "Incorrect number in result")
    assertEquals(initial.toSet(), tour.toSet(), "Different sets")
    assertEquals(fixedPointsBefore, fixedPointsAfter, "Different fixed points")

    return distance to duration
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