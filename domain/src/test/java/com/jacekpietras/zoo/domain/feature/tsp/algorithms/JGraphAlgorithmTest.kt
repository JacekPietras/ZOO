package com.jacekpietras.zoo.domain.feature.tsp.algorithms

import com.jacekpietras.zoo.domain.feature.tsp.TravelingSalesmanProblemAlgorithm
import kotlinx.coroutines.test.runTest
import org.jgrapht.alg.tour.*
import org.junit.jupiter.api.Test

internal class JGraphAlgorithmTest {

    @Test
    fun `Annealing test over 15`() {
        doTest(
            seed = 1000,
            numberOfCities = 15,
            bestExpected = 1579.2105821724608,
        )
    }

    @Test
    fun `Annealing test over 30`() {
        doTest(
            seed = 2000,
            numberOfCities = 30,
            bestExpected = 2138.93320969663,
        )
    }
    @Test
    fun `Annealing test over 60`() {
        doTest(
            seed = 2000,
            numberOfCities = 60,
            bestExpected = 3064.144665909665,
        )
    }

    private fun doTest(seed: Long, numberOfCities: Int, bestExpected: Double = 0.0) = runTest {
        algorithms.forEach { (name, algorithm) ->
            println("\n--- $name ---")
            doTspTest(
                algorithm = algorithm,
                seed = seed,
                numberOfCities = numberOfCities,
                bestExpected = bestExpected,
                times = 100,
            )
        }
    }

    companion object {

        val algorithms = listOf<Pair<String, TravelingSalesmanProblemAlgorithm<City>>>(
//                HeldKarpTSP() //requires a lot of memory
//                ChristofidesThreeHalvesApproxMetricTSP() // no class def found error

            "Greedy" to JGraphTSPAlgorithm<City>(GreedyHeuristicTSP()).let(::DivorcedTSP),
            "NearestInsertion" to JGraphTSPAlgorithm<City>(NearestInsertionHeuristicTSP()).let(::DivorcedTSP),
            "NearestNeighbor" to JGraphTSPAlgorithm<City>(NearestNeighborHeuristicTSP()).let(::DivorcedTSP),
            "TwoApprox" to JGraphTSPAlgorithm<City>(TwoApproxMetricTSP()).let(::DivorcedTSP),
            "TwoOpt (1, near)" to JGraphTSPAlgorithm<City>(TwoOptHeuristicTSP(1, NearestNeighborHeuristicTSP())).let(::DivorcedTSP),
            "TwoOpt (100, near)" to JGraphTSPAlgorithm<City>(TwoOptHeuristicTSP(100, NearestNeighborHeuristicTSP())).let(::DivorcedTSP),
            "TwoOpt (1, rnd)" to JGraphTSPAlgorithm<City>(TwoOptHeuristicTSP(1)).let(::DivorcedTSP),
            "TwoOpt (100, rnd)" to JGraphTSPAlgorithm<City>(TwoOptHeuristicTSP(100)).let(::DivorcedTSP),

            "SimulatedAnnealing" to SimulatedAnnealing(),
        )
    }
}