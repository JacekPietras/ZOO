package com.jacekpietras.zoo.domain.feature.tsp.algorithms

import com.jacekpietras.zoo.domain.feature.tsp.TravelingSalesmanProblemAlgorithm
import org.jgrapht.alg.interfaces.HamiltonianCycleAlgorithm
import org.jgrapht.graph.DefaultWeightedEdge

internal class JGraphTSPAlgorithm<T : Any>(
    private val algorithm: HamiltonianCycleAlgorithm<T,DefaultWeightedEdge>
) : TravelingSalesmanProblemAlgorithm<T> {

    override suspend fun run(
        points: List<T>,
        distanceCalculation: suspend (T, T) -> Double,
        immutablePositions: List<Int>?,
    ): Pair<Double, List<T>> {
        val graph = points.toJGraph(distanceCalculation)

        val tour = algorithm.getTour(graph)

        println(tour.weight)

        return 0.0 to tour.vertexList
    }
}
