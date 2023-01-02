package com.jacekpietras.zoo.domain.feature.pathfinder

import com.jacekpietras.geometry.PointD
import com.jacekpietras.zoo.domain.feature.pathfinder.model.MinEdge
import com.jacekpietras.zoo.domain.feature.pathfinder.model.MinNode
import com.jacekpietras.zoo.domain.feature.pathfinder.model.Node
import com.jacekpietras.zoo.domain.feature.pathfinder.model.SnappedOnMin
import com.jacekpietras.zoo.domain.feature.pathfinder.model.SnappedOnMin.SnappedOnMinEdge
import kotlinx.coroutines.delay
import kotlin.contracts.contract

internal class MinGraphAnalyzer {

    private var nodes: Collection<MinNode>? = null
    private val snapper = PointSnapper()

    fun initialize(fullNodes: Set<Node>) {
        val mappedNodes = fullNodes.map { MinNode(it.point) }.toMutableSet()
        fullNodes.forAllEdges { a, b, technical, weight ->
            val minA = mappedNodes.first { a.point == it.point }
            val minB = mappedNodes.first { b.point == it.point }
            minA.connect(minB, technical, weight, true)
            minB.connect(minA, technical, weight, false)
        }
        val (corners, minNodes) = mappedNodes.separateCorners()
        corners.forEach { corner ->
            corner.straightenCorner()
        }

        nodes = minNodes
    }

    suspend fun getShortestPath(
        endPoint: PointD,
        startPoint: PointD?,
        technicalAllowedAtStart: Boolean = true,
        technicalAllowedAtEnd: Boolean = false,
    ): List<PointD> {
        // fixme calculate in min and full graph and store calculation times. print current + avg

        val nodes = waitForNodes()

        if (startPoint == null) return listOf(endPoint)
        if (nodes.isEmpty()) return listOf(endPoint)

        println(nodes.joinToString("\n") { "[" + it.point.x + ", " + it.point.y + "]" + "\nedges:\n" + it.edges.joinToString("\n") + "\n" })

        val snapStart = snapper.getSnappedOnMinEdge(nodes, startPoint, technicalAllowed = technicalAllowedAtStart)
        val snapEnd = snapper.getSnappedOnMinEdge(nodes, endPoint, technicalAllowed = technicalAllowedAtEnd)

//        if (onSameEdge(snapStart, snapEnd)) {
//            return resultFromTheSameEdge(snapStart, snapEnd)
//        } else if (onReversedEdge(snapStart, snapEnd)) {
//            throw IllegalStateException("With current implementation of snapping, shouldn't happen, might implement in future")
//        } else {
        val result = getShortestPathJob(
            start = snapStart,
            end = snapEnd,
            technicalAllowed = technicalAllowedAtEnd,
        )
//        println("result before filling gaps: ${result.map { "(" + it.point.x + ", " + it.point.y + ")" }}") // fixme remove
        return result.pointPath(snapEnd)
//        }
    }

//    private fun resultFromTheSameEdge(
//        snapStart: SnappedOnMinEdge,
//        snapEnd: SnappedOnMinEdge
//    ): List<PointD> {
//        val corners = if (snapStart.weightFromStart < snapEnd.weightFromStart) {
//            snapStart.edge.corners
//                .filter { (_, weight) -> snapStart.weightFromStart < weight && weight < snapEnd.weightFromStart }
//        } else {
//            snapStart.edge.corners
//                .filter { (_, weight) -> snapEnd.weightFromStart < weight && weight < snapStart.weightFromStart }
//                .reversed()
//        }
//        return listOf(snapStart.point) + corners.map { (p, _) -> p } + snapEnd.point
//    }
//
//    private fun onSameEdge(
//        snapStart: SnappedOnMin,
//        snapEnd: SnappedOnMin
//    ): Boolean {
//        contract {
//            returns(true) implies (snapStart is SnappedOnMinEdge)
//            returns(true) implies (snapEnd is SnappedOnMinEdge)
//        }
//        return snapStart is SnappedOnMinEdge && snapEnd is SnappedOnMinEdge &&
//                snapStart.edge == snapEnd.edge
//    }
//
//    private fun onReversedEdge(
//        snapStart: SnappedOnMin,
//        snapEnd: SnappedOnMin
//    ): Boolean {
//        contract {
//            returns(true) implies (snapStart is SnappedOnMinEdge)
//            returns(true) implies (snapEnd is SnappedOnMinEdge)
//        }
//        return snapStart is SnappedOnMinEdge && snapEnd is SnappedOnMinEdge &&
//                (snapStart.edge.from == snapEnd.edge.node && snapStart.edge.node == snapEnd.edge.from &&
//                        snapStart.cornerPoints == snapEnd.cornerPoints.reversed())
//    }
//
//    private val SnappedOnMinEdge.cornerPoints
//        get() = edge.corners.map(Pair<PointD, Double>::first)

    private suspend fun getShortestPathJob(
        start: SnappedOnMin,
        end: SnappedOnMin,
        technicalAllowed: Boolean = false,
    ): List<MinNode> =
        MinDijkstra(
            vertices = waitForNodes(),
            technicalAllowed = technicalAllowed,
        ).calculate(
            start = start,
            end = end,
        )

    internal suspend fun waitForNodes(): Collection<MinNode> {
        while (nodes == null) {
            delay(100)
        }
        return checkNotNull(nodes)
    }

    private fun Collection<MinNode>.separateCorners() =
        partition { node ->
            val edges = node.edges.distinctBy { it.node.point }
            edges.size == 2 && edges.first().technical == edges.last().technical
        }

    private fun MinNode.straightenCorner() {
        val e1 = edges.first()
        val e2 = edges.last()
        val from = e1.node
        val to = e2.node
        val weight = e1.weight + e2.weight
        val corners = e1.corners.reversed().map { (p, pWeight) -> p to e1.weight - pWeight } +
                (point to e1.weight) +
                e2.corners.map { (p, pWeight) -> p to e1.weight + pWeight }
        val cornersReversed = corners.reversed().map { (p, pWeight) -> p to weight - pWeight }

        from.edges.add(MinEdge(to, from, e1.technical, weight, true, corners))
        to.edges.add(MinEdge(from, to, e1.technical, weight, false, cornersReversed))

        from.edges.remove(from.edges.first { it.node == this })
        to.edges.remove(to.edges.first { it.node == this })
    }

    private fun List<MinNode>.pointPath(snapEnd: SnappedOnMin): List<PointD> =
        zipWithNext { a, b ->
            listOf(a.point) + a.edges.find { it.node == b }.cornerPoints()
        }.flatten() +

                last().point

//                when (snapEnd) {
//            is SnappedOnMin.SnappedOnMinNode -> {
//                listOf(last().point)
//            }
//            is SnappedOnMinEdge -> {
//                when (this[this.lastIndex - 1]) {
//                    snapEnd.edge.from -> {
//                        val cornersBeforeSnapped = snapEnd.edge.corners
//                            .filter { (_, weight) -> weight < snapEnd.weightFromStart }
//                            .map { (point, _) -> point }
//                        (cornersBeforeSnapped + last().point).distinct()
//                    }
//                    snapEnd.edge.node -> {
//                        val cornersAfterSnapped = snapEnd.edge.corners
//                            .filter { (_, weight) -> weight > snapEnd.weightFromStart }
//                            .map { (point, _) -> point }
//                            .reversed()
//                        (cornersAfterSnapped + last().point).distinct()
//                    }
//                    else -> throw IllegalStateException("point before ending should be on end of ending edge")
//                }
//            }
//        }

    private fun MinEdge?.cornerPoints() =
        this?.corners?.map(Pair<PointD, Double>::first) ?: emptyList()
}
