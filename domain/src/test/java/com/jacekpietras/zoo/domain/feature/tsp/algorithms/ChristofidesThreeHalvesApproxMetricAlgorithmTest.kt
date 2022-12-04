package com.jacekpietras.zoo.domain.feature.tsp.algorithms

import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

internal class ChristofidesThreeHalvesApproxMetricAlgorithmTest{

    @Test
    fun `Annealing test over 15`() {
        repeat(100) {
            doTest(
                seed = 1000,
                numberOfCities = 15,
                bestExpected = 2213.471145583158,
            )
        }
    }

    @Test
    fun `Annealing test over 30`() {
        repeat(100) {
            doTest(
                seed = 2000,
                numberOfCities = 30,
                bestExpected = 4861.186475289512,
            )
        }
    }

    private fun doTest(seed: Long, numberOfCities: Int, bestExpected: Double = 0.0) = runTest {
        doTspTest(
            algorithm = ChristofidesThreeHalvesApproxMetricAlgorithm(),
            seed = seed,
            numberOfCities = numberOfCities,
            bestExpected = bestExpected,
        )
    }
}