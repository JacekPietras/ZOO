package com.jacekpietras.zoo.domain.feature.vrp.algorithms

import com.jacekpietras.zoo.domain.feature.vrp.VRPAlgorithm
import org.jgrapht.Graph
import org.jgrapht.alg.interfaces.HamiltonianCycleAlgorithm
import org.jgrapht.graph.DefaultWeightedEdge
import org.jgrapht.graph.WeightedMultigraph

internal class JGraphTSPAlgorithm<T : Any>(
    private val algorithm: HamiltonianCycleAlgorithm<T, DefaultWeightedEdge>
) : VRPAlgorithm<T> {

    override suspend fun run(
        points: List<T>,
        distanceCalculation: suspend (T, T) -> Double,
    ): List<T> {
        val graph = points.toJGraph(distanceCalculation)
        return algorithm.getTour(graph).vertexList
    }

    private suspend fun <T : Any> List<T>.toJGraph(
        distanceCalculation: suspend (T, T) -> Double,
    ): Graph<T, DefaultWeightedEdge> {
        val graph = WeightedMultigraph<T, DefaultWeightedEdge>(DefaultWeightedEdge::class.java)

        this.forEach(graph::addVertex)

        val pointsTo = this.toMutableList()
        this.forEach { from ->
            pointsTo.remove(from)
            pointsTo.forEach { to ->
                graph.addEdge(from, to)
                graph.setEdgeWeight(from, to, distanceCalculation(from, to))
            }
        }

        return graph
    }
}
