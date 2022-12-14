package com.jacekpietras.zoo.domain.feature.tsp.algorithms

import com.jacekpietras.zoo.domain.feature.tsp.DivorcedTSPAlgorithm
import com.jacekpietras.zoo.domain.feature.tsp.TSPAlgorithm
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

        val algorithms = listOf<Pair<String, TSPWithFixedStagesAlgorithm<City>>>(
//                HeldKarpTSP() //requires a lot of memory
//                ChristofidesThreeHalvesApproxMetricTSP() // no class def found error

            "Greedy" to divorcedTSP(GreedyHeuristicTSP()),
            "NearestInsertion" to divorcedTSP(NearestInsertionHeuristicTSP()),
            "NearestNeighbor" to divorcedTSP(NearestNeighborHeuristicTSP()),
            "TwoApprox" to divorcedTSP(TwoApproxMetricTSP()),
            "TwoOpt (1, near)" to divorcedTSP(TwoOptHeuristicTSP(1, NearestNeighborHeuristicTSP())),
            "TwoOpt (100, near)" to divorcedTSP(TwoOptHeuristicTSP(100, NearestNeighborHeuristicTSP())),
            "TwoOpt (1, rnd)" to divorcedTSP(TwoOptHeuristicTSP(1)),
            "TwoOpt (100, rnd)" to divorcedTSP(TwoOptHeuristicTSP(100)),

            "SimulatedAnnealing" to SimulatedAnnealing(),
        )

        private fun divorcedTSP(algorithm: HamiltonianCycleAlgorithm<City, DefaultWeightedEdge>) =
            DivorcedTSPAlgorithm(JGraphTSPAlgorithm(algorithm), City(-1, -1))
    }
}