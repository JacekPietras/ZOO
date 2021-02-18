package com.jacekpietras.zoo.domain.business

import com.jacekpietras.core.PointD
import com.jacekpietras.zoo.domain.model.MapItemEntity.PathEntity

class GraphAnalyzer(roads: List<PathEntity>) {

    private val nodes = mutableSetOf<Node>()

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
    }

    fun getTerminalPoints(): List<PointD> =
        nodes
            .asSequence()
            .filter { it.edges.size <= 1 }
            .map { it.point }
            .toList()

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

    private class Node(
        val point: PointD,
        val edges: MutableSet<Node> = mutableSetOf()
    ) {

        fun connect(node: Node) {
            edges.add(node)
        }
    }
}