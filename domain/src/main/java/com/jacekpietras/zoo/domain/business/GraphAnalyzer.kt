package com.jacekpietras.zoo.domain.business

import com.jacekpietras.core.PointD
import com.jacekpietras.core.pow2
import com.jacekpietras.zoo.domain.model.MapItemEntity.PathEntity
import timber.log.Timber
import kotlin.math.abs
import kotlin.math.sqrt

class GraphAnalyzer(roads: List<PathEntity>) {

    private val nodes = mutableSetOf<Node>()
//    private val overLine = mutableSetOf<Node>()

    init {
        roads.forEach { path ->
            var prevNode: Node? = null
            path.vertices
                .forEach { point ->
                    val nextNode = addToGraph(point)
                    if (prevNode != null) {
                        nextNode.connect(prevNode!!)
                        prevNode!!.connect(nextNode)
                    }

                    prevNode = nextNode
                }
        }

        checkNodesOnEdges()
    }

    fun getTerminalPoints(): List<PointD> =
        nodes
            .asSequence()
            .filter { it.edges.size <= 1 }
            .map { it.point }
            .toList()

//    fun getOverLine(): List<PointD> =
//        overLine
//            .map { it.point }
//            .toList()

    private fun checkNodesOnEdges() {
        nodes.forEach { first ->
            checkNodesOnEdges(first)
        }
    }

    private fun checkNodesOnEdges(first: Node) {
        nodes.forEach { second ->
            if (second != first) {
                second.edges.forEach { third ->
                    if (third != first) {
                        if (isOnEdge(first, second, third)) {
                            Timber.e("dupa found $second $third")
//                            overLine.add(first)
                            first.connect(second)
                            first.connect(third)
                            second.connect(first)
                            third.connect(first)
                            second.disconnect(third)
                            third.disconnect(second)
                            return
                        }
                    }
                }
            }
        }
    }

    private fun addToGraph(point: PointD): Node {
        nodes.forEach { node ->
            if (node.point == point) {
                return node
            }
        }
        val newNode = Node(point)
        nodes.add(newNode)
        return newNode
    }

    private fun isOnEdge(point: Node, edge1: Node, edge2: Node): Boolean =
         distance(edge1, point) + distance(edge2, point) - distance(edge1, edge2) < 0.000000001

    private fun distance(a: Node, b: Node): Double =
        sqrt( (a.x-b.x).pow2 + (a.y-b.y).pow2 )

    private fun distanceFromLine(point: Node, edge1: Node, edge2: Node): Double {
        val A = point.x - edge1.x // position of point rel one end of line
        val B = point.y - edge1.y
        val C = edge2.x - edge1.x // vector along line
        val D = edge2.y - edge1.y
        val E = -D // orthogonal vector
        val dot = A * E + B * C
        val len_sq = E * E + C * C
        return abs(dot) / sqrt(len_sq)
//        return dot * dot / len_sq // faster
    }

    private class Node(
        val point: PointD,
        val edges: MutableSet<Node> = mutableSetOf()
    ) {

        val x: Double
            get() = point.x

        val y: Double
            get() = point.y

        fun connect(node: Node) {
            edges.add(node)
        }

        fun disconnect(node: Node) {
            edges.remove(node)
        }

        override fun toString() = "PointD($x, $y),"
    }
}