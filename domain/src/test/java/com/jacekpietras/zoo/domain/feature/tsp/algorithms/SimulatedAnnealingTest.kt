package com.jacekpietras.zoo.domain.feature.tsp.algorithms

import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

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

    private fun doAnnealingTest(seed: Long, numberOfCities: Int, bestExpected: Double = 0.0) = runTest {
        doTspTest(
            algorithm = SimulatedAnnealing(),
            seed = seed,
            numberOfCities = numberOfCities,
            bestExpected = bestExpected,
        )
    }
}
