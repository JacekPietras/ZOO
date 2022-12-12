package com.jacekpietras.zoo.domain.feature.tsp.algorithms

import kotlinx.coroutines.runBlocking
import org.jgrapht.Graph
import org.jgrapht.graph.DefaultWeightedEdge
import org.jgrapht.graph.WeightedMultigraph

internal suspend fun <T : Any> List<T>.toJGraph(
    distanceCalculation: suspend (T, T) -> Double,
): Graph<T, DefaultWeightedEdge> {
    val graph = WeightedMultigraph<T, DefaultWeightedEdge>(DefaultWeightedEdge::class.java)

    this.forEach(graph::addVertex)
    val pointsTo = this.toMutableList()
    this.forEach { from ->
        pointsTo.remove(from)
        pointsTo.forEach { to ->
            graph.addEdge(from, to)
            graph.setEdgeWeight(from, to, distanceCalculation(from, to) )
        }
    }

    return graph
}

// data class MyEdge<T : Any>(
//    val from: T,
//    val to: T,
//    private val distanceCalculation: suspend (T, T) -> Double,
//) : DefaultWeightedEdge() {
//
//    override fun getSource(): Any {
//        return from
//    }
//
//    override fun getTarget(): Any {
//        return to
//    }
//
//    override fun getWeight(): Double {
//        return runBlocking {
//            distanceCalculation(from, to)
//        }
//    }

//    override val weight:Double
//    get() =  runBlocking {
//        distanceCalculation(from, to)
//    }
//}
