package com.jacekpietras.zoo.domain.feature.tsp.algorithms

import com.jacekpietras.zoo.domain.feature.tsp.DivorcedTSPAlgorithm
import com.jacekpietras.zoo.domain.feature.tsp.TSPWithFixedStagesAlgorithm
import kotlinx.coroutines.test.runTest
import org.jgrapht.alg.interfaces.HamiltonianCycleAlgorithm
import org.jgrapht.alg.tour.*
import org.jgrapht.graph.DefaultWeightedEdge
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
            bestExpected = 3062.6420210301117,
        )
    }

    @Test
    fun `Annealing test with fixed positions on edges`() {
        doTest(
            seed = 1000,
            numberOfCities = 15,
            bestExpected = 1719.785146459187,
            immutablePositions = listOf(0, 14),
        )
    }

    @Test
    fun `Annealing test with fixed positions on begin`() {
        doTest(
            seed = 1000,
            numberOfCities = 15,
            bestExpected = 1685.7027703853232,
            immutablePositions = listOf(0),
        )
    }

    @Test
    fun `Annealing test with fixed positions on end`() {
        doTest(
            seed = 1000,
            numberOfCities = 15,
            bestExpected = 1689.001101946651,
            immutablePositions = listOf(14),
        )
    }

    private fun doTest(
        seed: Long,
        numberOfCities: Int,
        bestExpected: Double = 0.0,
        immutablePositions: List<Int>? = null,
    ) = runTest {
        algorithms.forEach { (name, algorithm) ->
            println("\n--- $name ---")
            doTspTest(
                algorithm = algorithm,
                seed = seed,
                numberOfCities = numberOfCities,
                bestExpected = bestExpected,
                times = 100,
                immutablePositions = immutablePositions,
            )
        }
    }

    companion object {

        val algorithms = listOf<Pair<String, TSPWithFixedStagesAlgorithm<City>>>(
            // requires a lot of memory
            // O(2^V * V^2)
            // "HeldKarp" to divorcedTSP(HeldKarpTSP()),

            // requires Triangle inequality (cannot hack with ZERO/MAX weights)
            // O(V^3 * E)
            // "Christofides" to divorcedTSP(ChristofidesThreeHalvesApproxMetricTSP()),

            // don't work with immutable and results are random
            // requires Triangle inequality (cannot hack with ZERO/MAX weights)
            // O(V^2 * log(V))
            // "TwoApprox" to divorcedTSP(TwoApproxMetricTSP()),

            // O(V^2 * log(V))
            "Greedy" to divorcedTSP(GreedyHeuristicTSP()),

            // O(V^2) (runtime)
            "NearestInsertion" to divorcedTSP(NearestInsertionHeuristicTSP()),

            // O(V^2) (runtime)
            "NearestNeighbor" to divorcedTSP(NearestNeighborHeuristicTSP()),

            // Optimization runs
            "TwoOpt (1, near)" to divorcedTSP(TwoOptHeuristicTSP(1, NearestNeighborHeuristicTSP())),
            "TwoOpt (10, near)" to divorcedTSP(TwoOptHeuristicTSP(10, NearestNeighborHeuristicTSP())),
            "TwoOpt (1, rnd)" to divorcedTSP(TwoOptHeuristicTSP(1)),
            "TwoOpt (10, rnd)" to divorcedTSP(TwoOptHeuristicTSP(10)),

            // Genetic
            "SimulatedAnnealing" to SimulatedAnnealing(),
        )

        private fun divorcedTSP(algorithm: HamiltonianCycleAlgorithm<City, DefaultWeightedEdge>) =
            DivorcedTSPAlgorithm(JGraphTSPAlgorithm(algorithm), City(-1, -1))
    }
}