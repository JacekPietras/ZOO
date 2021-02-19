package com.jacekpietras.zoo.domain.business

import com.jacekpietras.core.PointD
import com.jacekpietras.core.pow2
import com.jacekpietras.zoo.domain.model.MapItemEntity.PathEntity
import timber.log.Timber
import kotlin.math.sqrt

object GraphAnalyzer {

    private val nodes = mutableSetOf<Node>()

    fun initialize(roads: List<PathEntity>, technical: List<PathEntity>) {
        nodes.clear()

        addAllToGraph(roads, technical = false)
        addAllToGraph(technical, technical = true)

        checkNodesOnEdges()
    }

    private fun addAllToGraph(list: List<PathEntity>, technical: Boolean) {
        list.forEach { path ->
            var prevNode: Node? = null
            path.vertices
                .forEach { point ->
                    val nextNode = addToGraph(point)
                    if (prevNode != null) {
                        nextNode.connect(prevNode!!, technical)
                        prevNode!!.connect(nextNode, technical)
                    }

                    prevNode = nextNode
                }
        }
    }

    fun getTerminalPoints(): List<PointD> =
        nodes
            .asSequence()
            .filter { it.edges.size > 2 }
//            .filter { it.edges.size <= 1 }
            .map { it.point }
            .toList()

    private fun checkNodesOnEdges() {
        nodes.forEach { first ->
            checkNodesOnEdges(first)
        }
    }

    private fun checkNodesOnEdges(first: Node) {
        nodes.forEach { second ->
            if (second != first) {
                second.edges.forEach { twoToThird ->
                    val third = twoToThird.node
                    if (third != first) {
                        if (isOnEdge(first, second, third)) {
                            Timber.e("dupa found $second $third")

                            first.connect(second, technical = twoToThird.technical)
                            first.connect(third, technical = twoToThird.technical)
                            second.connect(first, technical = twoToThird.technical)
                            third.connect(first, technical = twoToThird.technical)
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
        sqrt((a.x - b.x).pow2 + (a.y - b.y).pow2)

//    private fun distanceFromLine(point: Node, edge1: Node, edge2: Node): Double {
//        val A = point.x - edge1.x // position of point rel one end of line
//        val B = point.y - edge1.y
//        val C = edge2.x - edge1.x // vector along line
//        val D = edge2.y - edge1.y
//        val E = -D // orthogonal vector
//        val dot = A * E + B * C
//        val len_sq = E * E + C * C
//        return abs(dot) / sqrt(len_sq)
////        return dot * dot / len_sq // faster
//    }

    private class Node(
        val point: PointD,
        val edges: MutableSet<Edge> = mutableSetOf()
    ) {

        val x: Double
            get() = point.x

        val y: Double
            get() = point.y

        fun connect(node: Node, technical: Boolean) {
            edges.add(Edge(node, technical))
        }

        fun disconnect(node: Node) {
            edges.remove(edges.first { it.node == node })
        }

        override fun toString() = "PointD($x, $y),"
    }

    private class Edge(
        val node: Node,
        val technical: Boolean,
        var length: Double = 0.0,
    )
}