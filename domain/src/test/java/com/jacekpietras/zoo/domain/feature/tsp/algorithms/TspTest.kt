package com.jacekpietras.zoo.domain.feature.tsp.algorithms

import com.jacekpietras.zoo.domain.feature.tsp.TravelingSalesmanProblemAlgorithm
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
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
        val tour = algorithm.run(
            points = initial,
            distanceCalculation = { a, b -> a.distanceToCity(b) },
        ).second

        assertEquals(initial.size, tour.size, "Incorrect number in result")

        result = tour.distance()
    }

    if (result < bestExpected) {
        println("New record: $result")
    }

    println("${initialDistance.toInt()} / ${result.toInt()} / ${bestExpected.toInt()} | in $duration")
}

internal class DivorcedTSP(private val algorithm: TravelingSalesmanProblemAlgorithm<City>) : TravelingSalesmanProblemAlgorithm<City> {

    override suspend fun run(
        points: List<City>,
        distanceCalculation: suspend (City, City) -> Double,
        immutablePositions: List<Int>?
    ): Pair<Double, List<City>> {
        val tour =
            algorithm.run(
                points = points + Dummy,
                distanceCalculation = { a, b ->
                    when {
                        a is Dummy -> 0.0
                        b is Dummy -> 0.0
                        else -> a.distanceToCity(b)
                    }
                },
            ).second
        val indexOfDummy = tour.indexOfFirst { it is Dummy }
        val begin = tour.subList(0, indexOfDummy)
        val end = tour.subList(indexOfDummy + 1, tour.lastIndex)
        val connected = end + begin
        return connected.distance() to connected
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

internal open class City(
    var x: Int,
    var y: Int,
) {

    fun distanceToCity(city: City): Double =
        sqrt(
            abs(x - city.x).toDouble().pow(2.0) +
                    abs(y - city.y).toDouble().pow(2.0)
        )
}

internal object Dummy : City(0, 0)