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
        // fixme calculate in min and full graph and store calculation times. print current + avg

        val nodes = waitForNodes()

        if (startPoint == null) return listOf(endPoint)
        if (nodes.isEmpty()) return listOf(endPoint)

//        println(nodes.joinToString("\n") { "[" + it.point.x + ", " + it.point.y + "]" + "\nedges:\n" + it.edges.joinToString("\n") + "\n" })

        val snapStart = snapper.getSnappedOnMinEdge(nodes, startPoint, technicalAllowed = technicalAllowedAtStart)
        val snapEnd = snapper.getSnappedOnMinEdge(nodes, endPoint, technicalAllowed = technicalAllowedAtEnd)

        val result = getShortestPathJob(
            start = snapStart,
            end = snapEnd,
            technicalAllowed = technicalAllowedAtEnd,
        )
        return result.pointPath(snapStart, snapEnd)
    }

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

    private fun List<MinNode>.pointPath(snapStart: SnappedOnMin, snapEnd: SnappedOnMin): List<PointD> =
        pointPathBegin(snapStart) + pointPathMiddle() + pointPathEnd(snapEnd, snapStart)

    private fun List<MinNode>.pointPathBegin(snapStart: SnappedOnMin): List<PointD> {
        val firstFromResult = first()
        return if (snapStart is SnappedOnMinEdge && firstFromResult.point != snapStart.point) {
            (listOf(snapStart.point) +
                    when {
                        snapStart.edge.from == snapStart.edge.node -> {
                            if (snapStart.weightFromStart < snapStart.edge.weight - snapStart.weightFromStart) {
                                cornersBeforeSnapped(snapStart)
                            } else {
                                cornersAfterSnapped(snapStart)
                            }
                        }
                        snapStart.edge.from == firstFromResult -> cornersBeforeSnapped(snapStart)
                        snapStart.edge.node == firstFromResult -> cornersAfterSnapped(snapStart)
                        else -> throw IllegalStateException("first point of result is not part of starting edge")
                    }).distinct()
        } else {
            emptyList()
        }
    }

    private fun List<MinNode>.pointPathMiddle() =
        zipWithNext { a, b -> listOf(a.point) + a.edges.find { it.node == b }.cornerPoints() }
            .flatten()

    private fun List<MinNode>.pointPathEnd(snapEnd: SnappedOnMin, snapStart: SnappedOnMin) =
        if (snapEnd is SnappedOnMinEdge && size > 1) {
            val nodeBeforeEnd = this[lastIndex - 1]
            when {
                nodeBeforeEnd == snapEnd.edge.from && nodeBeforeEnd == snapEnd.edge.node -> {
                    if (snapEnd.weightFromStart < snapEnd.edge.weight - snapEnd.weightFromStart) {
                        (cornersBeforeSnapped(snapEnd) + last().point).distinct()
                    } else {
                        (cornersAfterSnapped(snapEnd) + last().point).distinct()
                    }
                }
                nodeBeforeEnd == snapEnd.edge.from -> {
                    (cornersBeforeSnapped(snapEnd) + last().point).distinct()
                }
                nodeBeforeEnd == snapEnd.edge.node -> {
                    (cornersAfterSnapped(snapEnd) + last().point).distinct()
                }
                snapStart is SnappedOnMinEdge && nodeBeforeEnd.point == snapStart.point -> {
                    cornersBetweenSnaps(snapStart, snapEnd) + last().point
                }
                else -> {
                    throw IllegalStateException("point before ending should be on end of ending edge")
                }
            }
        } else {
            listOf(last().point)
        }

    private fun cornersAfterSnapped(snap: SnappedOnMinEdge) = snap.edge.corners
        .filter { (_, weight) -> weight > snap.weightFromStart }
        .map { (point, _) -> point }
        .reversed()

    private fun cornersBeforeSnapped(snap: SnappedOnMinEdge) = snap.edge.corners
        .filter { (_, weight) -> weight < snap.weightFromStart }
        .map { (point, _) -> point }

    private fun MinEdge?.cornerPoints() =
        this?.corners?.map(Pair<PointD, Double>::first) ?: emptyList()

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
