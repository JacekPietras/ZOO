package com.jacekpietras.zoo.domain.feature.tsp.algorithms

import com.jacekpietras.zoo.domain.feature.tsp.TravelingSalesmanProblemAlgorithm
import kotlinx.coroutines.runBlocking
import org.jgrapht.Graph
import org.jgrapht.alg.tour.ChristofidesThreeHalvesApproxMetricTSP
import org.jgrapht.graph.DefaultWeightedEdge
import org.jgrapht.graph.WeightedMultigraph

internal class ChristofidesThreeHalvesApproxMetricAlgorithm<T:Any> : TravelingSalesmanProblemAlgorithm<T> {

    private val algorithm = ChristofidesThreeHalvesApproxMetricTSP<T, DefaultWeightedEdge>()

    override suspend fun run(
        points: List<T>,
        distanceCalculation: suspend (T, T) -> Double,
        immutablePositions: List<Int>?,
    ): Pair<Double, List<T>> {

        val g: Graph<T, DefaultWeightedEdge> = WeightedMultigraph(DefaultWeightedEdge::class.java)
        points.forEach(g::addVertex)
        points.forEach { from ->
            points.forEach { to ->
                if(from != to)
                g.addEdge(from, to, MyEdge(from, to, distanceCalculation) )
            }
        }

        val tour = algorithm.getTour(g)

        return 0.0 to tour.vertexList
    }
}

private class MyEdge<T : Any>(
    val from: T,
    val to: T,
    private val distanceCalculation: suspend (T, T) -> Double,
) : DefaultWeightedEdge() {

    override fun getSource(): Any {
        return from
    }

    override fun getTarget(): Any {
        return to
    }

    override fun getWeight(): Double {
        return runBlocking {
            distanceCalculation(from, to)
        }
    }
}

/*


Class Name 	                    Edges 	    Self-loops 	Multiple edges  Weighted
WeightedMultigraph 	            undirected 	no 	        yes             yes

 */