package com.jacekpietras.zoo.domain.feature.pathfinder

import com.jacekpietras.geometry.PointD
import com.jacekpietras.zoo.domain.feature.pathfinder.model.MinNode
import com.jacekpietras.zoo.domain.feature.pathfinder.model.Node
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.withLock

internal class MinGraphAnalyzer {

    private var nodes: Collection<MinNode>? = null

    fun initialize(fullNodes: Set<Node>) {
        val minNodes = fullNodes.map { MinNode(it.point) }
        fullNodes.forAllEdges { a, b, technical, weight ->
            val minA = minNodes.first { a.point == it.point }
            val minB = minNodes.first { b.point == it.point }
            minA.connect(minB, technical, weight)
            minB.connect(minA, technical, weight)
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

        val snapStart = nodes.first { it.point == startPoint }
        val snapEnd = nodes.first { it.point == endPoint }

        return getShortestPathJob(
            start = snapStart,
            end = snapEnd,
            technicalAllowed = technicalAllowedAtEnd,
        ).map { PointD(it.x, it.y) }
    }

    private suspend fun getShortestPathJob(
        start: MinNode,
        end: MinNode,
        technicalAllowed: Boolean = false,
    ): List<MinNode> =
        MinDijkstra(
            vertices = waitForNodes(),
            start = start,
            end = end,
            technicalAllowed = technicalAllowed
        ).getPath()

    internal suspend fun waitForNodes(): Collection<MinNode> {
        while (nodes == null) {
            delay(100)
        }
        return checkNotNull(nodes)
    }
}