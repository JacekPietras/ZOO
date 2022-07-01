package com.jacekpietras.zoo.domain.feature.pathfinder

import com.jacekpietras.zoo.domain.feature.tsp.SimulatedAnnealing
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt
import kotlin.random.Random
import kotlin.time.measureTime

class SimulatedAnnealingTest {

    @Test
    fun `Annealing test over 15`() {
        repeat(100) {
            doAnnealingTest(
                seed = 1000,
                numberOfCities = 15,
                bestExpected = 2213.471145583158,
            )
        }
    }

    @Test
    fun `Annealing test over 30`() {
        repeat(100) {
            doAnnealingTest(
                seed = 2000,
                numberOfCities = 30,
                bestExpected = 4861.186475289512,
            )
        }
    }

    private fun doAnnealingTest(seed: Long, numberOfCities: Int, bestExpected: Double) = runTest {
        val initial = generateInitialTravel(
            seed = seed,
            numberOfCities = numberOfCities,
        )
        val initialDistance = initial.distance()
        var result: Double

        val duration = measureTime {
            result = SimulatedAnnealing<City>().run(
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

    class City(
        var x: Int,
        var y: Int,
    ) {

        fun distanceToCity(city: City): Double =
            sqrt(
                abs(x - city.x).toDouble().pow(2.0) +
                        abs(y - city.y).toDouble().pow(2.0)
            )
    }
}