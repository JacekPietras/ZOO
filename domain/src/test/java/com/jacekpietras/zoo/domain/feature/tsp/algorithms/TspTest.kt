package com.jacekpietras.zoo.domain.feature.tsp.algorithms

import com.jacekpietras.zoo.domain.feature.tsp.TravelingSalesmanProblemAlgorithm
import org.junit.jupiter.api.Assertions.assertEquals
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt
import kotlin.random.Random
import kotlin.time.Duration
import kotlin.time.measureTime

internal suspend fun doTspTest(
    algorithm: TravelingSalesmanProblemAlgorithm<City>,
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
    val algorithmWithCounting = CountingTSP(algorithm)

    val (results, durations) = (1..times)
        .map { doTspTest(initial, algorithmWithCounting) }
        .unzip()

    val result = results.min()
    val duration = durations.min()

    if (result < bestExpected) {
        println("New record: $result")
    }
    println("${initialDistance.toInt()} / ${result.toInt()} / ${bestExpected.toInt()} | in $duration | ${algorithmWithCounting.calcCount} counts")
}

private suspend fun doTspTest(
    initial: List<City>,
    algorithm: TravelingSalesmanProblemAlgorithm<City>,
): Pair<Double, Duration> {
    var result: Double

    val duration = measureTime {
        val tour = algorithm.run(
            points = initial,
            distanceCalculation = { a, b -> a.distanceToCity(b) },
        ).second

        assertEquals(initial.size, tour.size, "Incorrect number in result")
        assertEquals(initial.toSet(), tour.toSet(), "Different sets")

        result = tour.distance()
    }

    return result to duration
}

internal class DivorcedTSP(private val algorithm: TravelingSalesmanProblemAlgorithm<City>) : TravelingSalesmanProblemAlgorithm<City> {

    override suspend fun run(
        points: List<City>,
        distanceCalculation: suspend (City, City) -> Double,
        immutablePositions: List<Int>?
    ): Pair<Double, List<City>> {
        val dummy = City(-1, -1)

        val tour =
            algorithm.run(
                points = points + dummy,
                distanceCalculation = { a, b ->
                    when {
                        a == dummy -> 0.0
                        b == dummy -> 0.0
                        else -> distanceCalculation(a, b)
                    }
                },
                immutablePositions = immutablePositions,
            ).second
        val indexOfDummy = tour.indexOfFirst { it == dummy }
        val begin = tour.subList(0, indexOfDummy)
        val end = tour.subList(indexOfDummy + 1, tour.lastIndex)
        val connected = end + begin
        return connected.distance() to connected
    }
}

internal class CountingTSP(private val algorithm: TravelingSalesmanProblemAlgorithm<City>) : TravelingSalesmanProblemAlgorithm<City> {

    var calcCount = 0

    override suspend fun run(
        points: List<City>,
        distanceCalculation: suspend (City, City) -> Double,
        immutablePositions: List<Int>?
    ): Pair<Double, List<City>> {
        return algorithm.run(
            points = points,
            distanceCalculation = { a, b ->
                calcCount++
                distanceCalculation(a, b)
            },
            immutablePositions = immutablePositions,
        )
    }
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