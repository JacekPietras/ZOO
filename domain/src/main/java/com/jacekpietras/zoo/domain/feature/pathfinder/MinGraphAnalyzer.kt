package com.jacekpietras.zoo.domain.feature.pathfinder

import com.jacekpietras.geometry.PointD
import com.jacekpietras.zoo.domain.feature.pathfinder.model.MinEdge
import com.jacekpietras.zoo.domain.feature.pathfinder.model.MinNode
import com.jacekpietras.zoo.domain.feature.pathfinder.model.Node
import com.jacekpietras.zoo.domain.feature.pathfinder.model.SnappedOnMin
import com.jacekpietras.zoo.domain.feature.pathfinder.model.SnappedOnMin.SnappedOnMinEdge
import kotlinx.coroutines.delay

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
        val nodes = waitForNodes()

        if (startPoint == null) return listOf(endPoint)
        if (nodes.isEmpty()) return listOf(endPoint)


        val snapStart = snapper.getSnappedOnMinEdges(nodes, startPoint, technicalAllowed = technicalAllowedAtStart)
        val snapEnd = snapper.getSnappedOnMinEdges(nodes, endPoint, technicalAllowed = technicalAllowedAtEnd)

//        println("Start: (" + snapStart.asNode().point.x.toInt() + "," + snapStart.asNode().point.y.toInt() + ")")
//        println("End  : (" + snapEnd.asNode().point.x.toInt() + "," + snapEnd.asNode().point.y.toInt() + ")\n\n")

        val result = getShortestPathJob(
            start = snapStart,
            end = snapEnd,
            technicalAllowed = technicalAllowedAtEnd,
        )
        return result.pointPath(snapStart, snapEnd)
    }

    private fun SnappedOnMin.asNode(): MinNode =
        when (this) {
            is SnappedOnMinEdge -> MinNode(point)
            is SnappedOnMin.SnappedOnMinNode -> node
        }

    private suspend fun getShortestPathJob(
        start: SnappedOnMin,
        end: SnappedOnMin,
        technicalAllowed: Boolean = false,
    ): List<PointD> =
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

    private fun List<PointD>.pointPath(snapStart: SnappedOnMin, snapEnd: SnappedOnMin): List<PointD> =
        (pointPathBegin(snapStart) + pointPathMiddle() + pointPathEnd(snapEnd, snapStart)).distinct()

    private fun List<PointD>.pointPathBegin(snapStart: SnappedOnMin): List<PointD> {
        val firstFromResult = first()
        return if (snapStart is SnappedOnMinEdge && firstFromResult != snapStart.point) {
            listOf(snapStart.point) +
                    when {
                        snapStart.edge.from == snapStart.edge.node -> {
                            if (snapStart.weightFromStart < snapStart.edge.weight - snapStart.weightFromStart) {
                                cornersBeforeSnapped(snapStart)
                            } else {
                                cornersAfterSnapped(snapStart)
                            }
                        }
                        snapStart.edge.from.point == firstFromResult -> cornersBeforeSnapped(snapStart)
                        snapStart.edge.node.point == firstFromResult -> cornersAfterSnapped(snapStart)
                        else -> throw IllegalStateException("first point of result is not part of starting edge")
                    }
        } else {
            emptyList()
        }
    }

    private fun List<PointD>.pointPathMiddle() =
//        zipWithNext { a, b -> listOf(a) + a.edges.find { it.node == b }.cornerPoints() }
//            .flatten()
        this.dropLast(1)//.drop(1)

    private fun List<PointD>.pointPathEnd(snapEnd: SnappedOnMin, snapStart: SnappedOnMin) =
        if (snapEnd is SnappedOnMinEdge && size > 1) {
            val nodeBeforeEnd = this[lastIndex - 1]
            when {
                nodeBeforeEnd == snapEnd.edge.from.point && nodeBeforeEnd == snapEnd.edge.node.point -> {
                    if (snapEnd.weightFromStart < snapEnd.edge.weight - snapEnd.weightFromStart) {
                        cornersBeforeSnapped(snapEnd) + last()
                    } else {
                        cornersAfterSnapped(snapEnd) + last()
                    }
                }
                nodeBeforeEnd == snapEnd.edge.from.point -> {
                    cornersBeforeSnapped(snapEnd) + last()
                }
                nodeBeforeEnd == snapEnd.edge.node.point -> {
                    cornersAfterSnapped(snapEnd) + last()
                }
                snapStart is SnappedOnMinEdge && nodeBeforeEnd == snapStart.point -> {
                    cornersBetweenSnaps(snapStart, snapEnd) + last()
                }
                else -> {
                    listOf(last())
                }
            }
        } else {
            listOf(last())
        }

    private fun cornersAfterSnapped(snap: SnappedOnMinEdge) = snap.edge.corners
        .filter { (_, weight) -> weight > snap.weightFromStart }
        .map { (point, _) -> point }

    private fun cornersBeforeSnapped(snap: SnappedOnMinEdge) = snap.edge.corners
        .filter { (_, weight) -> weight < snap.weightFromStart }
        .map { (point, _) -> point }

    private fun cornersBetweenSnaps(
        snapStart: SnappedOnMinEdge,
        snapEnd: SnappedOnMinEdge
    ): List<PointD> {
        val corners = if (snapStart.weightFromStart < snapEnd.weightFromStart) {
            snapStart.edge.corners
                .filter { (_, weight) -> snapStart.weightFromStart < weight && weight < snapEnd.weightFromStart }
        } else {
            snapStart.edge.corners
                .filter { (_, weight) -> snapEnd.weightFromStart < weight && weight < snapStart.weightFromStart }
                .reversed()
        }
        return corners.map { (p, _) -> p }
    }
}
