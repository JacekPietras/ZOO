package com.jacekpietras.zoo.domain.feature.tsp.algorithms

import com.jacekpietras.zoo.domain.feature.tsp.DivorcedTSPAlgorithm
import com.jacekpietras.zoo.domain.feature.tsp.TSPWithFixedStagesAlgorithm
import com.jacekpietras.zoo.domain.feature.tsp.plus
import kotlinx.coroutines.test.runTest
import org.jgrapht.alg.interfaces.HamiltonianCycleAlgorithm
import org.jgrapht.alg.tour.NearestNeighborHeuristicTSP
import org.jgrapht.alg.tour.TwoOptHeuristicTSP
import org.jgrapht.graph.DefaultWeightedEdge
import org.junit.jupiter.api.Test

internal class JGraphAlgorithmTest {

    @Test
    fun `optimization test over 15`() {
        doTest(
            seed = 1000,
            numberOfCities = 15,
            bestExpected = 1579.2105821724608,
        )
    }

    @Test
    fun `optimization test over 30`() {
        doTest(
            seed = 2000,
            numberOfCities = 30,
            bestExpected = 2138.93320969663,
        )
    }

    @Test
    fun `optimization test over 60`() {
        doTest(
            seed = 2000,
            numberOfCities = 60,
            bestExpected = 3062.6420210301117,
        )
    }

    @Test
    fun `optimization test over 50`() {
        doTest(
            seed = 3000,
            numberOfCities = 50,
            bestExpected = 2838.4030950314805,
        )
    }

    @Test
    fun `optimization test over 51`() {
        doTest(
            seed = 51,
            numberOfCities = 51,
            bestExpected = 2662.8134265520603,
        )
    }

    @Test
    fun `optimization test over 52`() {
        doTest(
            seed = 52,
            numberOfCities = 52,
            bestExpected = 2641.6033459501105,
        )
    }

    @Test
    fun `optimization test over 53`() {
        doTest(
            seed = 53,
            numberOfCities = 53,
            bestExpected = 2880.401282678936,
        )
    }

    @Test
    fun `optimization test over 54`() {
        doTest(
            seed = 54,
            numberOfCities = 54,
            bestExpected = 2743.4029487090984,
        )
    }

    @Test
    fun `optimization test over 55`() {
        doTest(
            seed = 55,
            numberOfCities = 55,
            bestExpected = 2406.8188761285646,
        )
    }

    @Test
    fun `optimization test over 56`() {
        doTest(
            seed = 56,
            numberOfCities = 56,
            bestExpected = 2977.1672645374324,
        )
    }

    @Test
    fun `optimization test over 57`() {
        doTest(
            seed = 57,
            numberOfCities = 57,
            bestExpected = 2611.979827420503,
        )
    }

    @Test
    fun `optimization test over 58`() {
        doTest(
            seed = 58,
            numberOfCities = 58,
            bestExpected = 3018.5138332809465,
        )
    }

    @Test
    fun `optimization test over 59`() {
        doTest(
            seed = 59,
            numberOfCities = 59,
            bestExpected = 2831.21536867656,
        )
    }

    @Test
    fun `optimization test with fixed positions on both ends`() {
        doTest(
            seed = 1000,
            numberOfCities = 15,
            bestExpected = 1719.785146459187,
            immutablePositions = listOf(0, 14),
        )
    }

    @Test
    fun `optimization test with fixed positions on both ends 60`() {
        doTest(
            seed = 7990,
            numberOfCities = 60,
            bestExpected = 3156.8089128986776,
            immutablePositions = listOf(0, 59),
        )
    }

    @Test
    fun `optimization test with fixed positions on begin`() {
        doTest(
            seed = 1000,
            numberOfCities = 15,
            bestExpected = 1685.7027703853232,
            immutablePositions = listOf(0),
        )
    }

    @Test
    fun `optimization test with fixed positions on end`() {
        doTest(
            seed = 1000,
            numberOfCities = 15,
            bestExpected = 1689.001101946651,
            immutablePositions = listOf(14),
        )
    }

    @Test
    fun `optimization test with fixed positions on in the middle`() {
        doTest(
            seed = 1000,
            numberOfCities = 15,
            bestExpected = 1918.4662028025448,
            immutablePositions = listOf(7),
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
            // "Greedy" to divorcedTSP(GreedyHeuristicTSP()),

            // O(V^2) (runtime)
            // Weak
            // "NearestInsertion" to divorcedTSP(NearestInsertionHeuristicTSP()),

            // O(V^2) (runtime)
            // "NearestNeighbor" to divorcedTSP(NearestNeighborHeuristicTSP()),

            // Optimization runs
            "TwoOpt (my)" to MyTwoOptHeuristicTSP(),
            "TwoOpt (1, near)" to divorcedTSP(TwoOptHeuristicTSP(1, NearestNeighborHeuristicTSP())),
            "TwoOpt (old)" to DivorcedTSPAlgorithm(MyOldTwoOptHeuristicTSP(), City(-1, -1)),
            // "TwoOpt (100, near)" to divorcedTSP(TwoOptHeuristicTSP(100, NearestNeighborHeuristicTSP())),
            // "TwoOpt (1, rnd)" to divorcedTSP(TwoOptHeuristicTSP(1)),
            // "TwoOpt (100, rnd)" to divorcedTSP(TwoOptHeuristicTSP(10)),

            // Genetic
            "SimulatedAnnealing" to SimulatedAnnealing(),
            "sum" to MyTwoOptHeuristicTSP<City>() + SimulatedAnnealing(),
        )

        private fun divorcedTSP(algorithm: HamiltonianCycleAlgorithm<City, DefaultWeightedEdge>) =
            DivorcedTSPAlgorithm(JGraphTSPAlgorithm(algorithm), City(-1, -1))
    }
}