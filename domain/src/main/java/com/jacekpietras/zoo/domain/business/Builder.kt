package com.jacekpietras.zoo.domain.business

import com.jacekpietras.core.PointD
import com.jacekpietras.core.haversine
import com.jacekpietras.zoo.domain.model.MapItemEntity
import timber.log.Timber

internal class Builder(
    roads: List<MapItemEntity.PathEntity>,
    technical: List<MapItemEntity.PathEntity>
) {

    private val nodes = mutableSetOf<Node>()

    init {
        nodes.clear()

        addAllToGraph(roads, technical = false)
        addAllToGraph(technical, technical = true)

        checkNodesOnEdges()

        calcDistances()
    }

    fun build() = nodes

    private fun addAllToGraph(list: List<MapItemEntity.PathEntity>, technical: Boolean) {
        list.forEach { path ->
            var prevNode: Node? = null
            path.vertices
                .forEach { point ->
                    val nextNode = addToGraph(point)
                    if (prevNode != null) {
                        nextNode.connect(prevNode!!, technical, backward = false)
                        prevNode!!.connect(nextNode, technical, backward = true)
                    }

                    prevNode = nextNode
                }
        }
    }

    private fun checkNodesOnEdges() {
        nodes.forEach { first ->
            checkNodesOnEdges(first)
        }
    }

    private fun checkNodesOnEdges(first: Node) {
        nodes.forEach { secnd ->
            if (secnd != first) {
                secnd.edges.forEach { twoToThird ->
                    val third = twoToThird.node
                    if (third != first) {
                        if (isOnEdge(first, secnd, third)) {
                            Timber.e("dupa found $secnd $third")

                            first.connect(secnd, twoToThird.technical, backward = false)
                            secnd.connect(first, twoToThird.technical, backward = true)
                            first.connect(third, twoToThird.technical, backward = false)
                            third.connect(first, twoToThird.technical, backward = true)
                            secnd.disconnect(third)
                            third.disconnect(secnd)
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
        cartesian(edge1, point) + cartesian(edge2, point) - cartesian(edge1, edge2) < 0.000000001

    private fun calcDistances() {
        nodes.forEach { node ->
            node.edges.forEach { edge ->
                edge.length = haversine(node.x, node.y, edge.x, edge.y)
            }
        }
    }
}