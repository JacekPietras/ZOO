package com.jacekpietras.zoo.domain.business

import com.jacekpietras.zoo.domain.feature.pathfinder.SimulatedAnnealing
import org.junit.jupiter.api.Test
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt
import kotlin.random.Random
import kotlin.time.ExperimentalTime
import kotlin.time.measureTime

@ExperimentalTime
class SimulatedAnnealingTest {

    @Test
    fun doATest() {
        val bestExpected = 2591.2322650196093
        val initial = generateInitialTravel(seed = 1000)
        val initialDistance = initial.distance()
        var result: Double

        val duration = measureTime {
            result = SimulatedAnnealing().simulateAnnealing(
                request = initial,
                distanceCalculation = { a, b -> a.distanceToCity(b) },
            ).first
        }

        println("$initialDistance / $result / $bestExpected | in $duration")
    }

    private fun generateInitialTravel(
        @Suppress("SameParameterValue") seed: Long,
        numberOfCities: Int = 15,
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