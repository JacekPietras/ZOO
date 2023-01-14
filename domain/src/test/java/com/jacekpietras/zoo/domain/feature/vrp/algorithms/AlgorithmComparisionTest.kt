package com.jacekpietras.zoo.domain.feature.vrp.algorithms

import com.jacekpietras.zoo.domain.feature.vrp.DivorcedVRPAlgorithm
import com.jacekpietras.zoo.domain.feature.vrp.VRPWithFixedStagesAlgorithm
import com.jacekpietras.zoo.domain.feature.vrp.plus
import com.jacekpietras.zoo.domain.feature.vrp.times
import kotlinx.coroutines.test.runTest
import org.jgrapht.alg.interfaces.HamiltonianCycleAlgorithm
import org.jgrapht.graph.DefaultWeightedEdge
import org.junit.jupiter.api.Test

internal class AlgorithmComparisionTest {

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
            bestExpected = 2833.6142142712047,
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
            bestExpected = 3090.897146641641,
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
            bestExpected = 1793.7921255545457,
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
            doVrpTest(
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

        val algorithms = listOf<Pair<String, VRPWithFixedStagesAlgorithm<City>>>(
            // requires a lot of memory
            // O(2^V * V^2)
            // "HeldKarp" to divorcedVRP(HeldKarpTSP()),

            // requires Triangle inequality (cannot hack with ZERO/MAX weights)
            // O(V^3 * E)
            // "Christofides" to divorcedVRP(ChristofidesThreeHalvesApproxMetricTSP()),

            // don't work with immutable and results are random
            // requires Triangle inequality (cannot hack with ZERO/MAX weights)
            // O(V^2 * log(V))
            // "TwoApprox" to divorcedVRP(TwoApproxMetricTSP()),

            // O(V^2 * log(V))
            // "Greedy" to divorcedVRP(GreedyHeuristicTSP()),

            // O(V^2) (runtime)
            // Weak
            // "NearestInsertion" to divorcedVRP(NearestInsertionHeuristicTSP()),

            // O(V^2) (runtime)
//            "NearestNeighbor (lib)" to divorcedVRP(NearestNeighborHeuristicTSP()),
//            "NearestNeighbor (my)" to NearestNeighbor(),

//            "TwoOpt (1, near)" to divorcedVRP(TwoOptHeuristicTSP(1, NearestNeighborHeuristicTSP())),
//            "TwoOpt (100, near)" to divorcedVRP(TwoOptHeuristicTSP(100, NearestNeighborHeuristicTSP())),
//            "TwoOpt (1, rnd)" to divorcedVRP(TwoOptHeuristicTSP(1)),
//            "TwoOpt (100, rnd)" to divorcedVRP(TwoOptHeuristicTSP(10)),
//            "TwoOpt (old)" to DivorcedTSPAlgorithm(MyOldTwoOptHeuristicTSP(), City(-1, -1)),
//            "TwoOpt (my)" to MyTwoOptHeuristicTSP(),
            "TwoOpt (my new)" to TwoOptHeuristicVRP(),

            "Lin-Kernighan" to MyLinKernighanVRP(),

            // Genetic
//            "SimulatedAnnealing" to SimulatedAnnealing(),

            // combinations
            "2opt + anne" to TwoOptHeuristicVRP<City>() + SimulatedAnnealing(),
            "nn + 2opt" to NearestNeighborVRP<City>() + TwoOptHeuristicVRP(),
            "2opt * (nn + 2opt)" to TwoOptHeuristicVRP<City>() * (NearestNeighborVRP<City>() + TwoOptHeuristicVRP()),
        )

        @Suppress("unused")
        private fun divorcedVRP(algorithm: HamiltonianCycleAlgorithm<City, DefaultWeightedEdge>) =
            DivorcedVRPAlgorithm(JGraphVRPAlgorithm(algorithm), City(-1, -1))
    }
}