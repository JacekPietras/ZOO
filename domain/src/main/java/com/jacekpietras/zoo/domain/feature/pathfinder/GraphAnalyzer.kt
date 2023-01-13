package com.jacekpietras.zoo.domain.feature.pathfinder

import com.jacekpietras.geometry.PointD
import com.jacekpietras.zoo.domain.feature.map.model.MapItemEntity.PathEntity
import com.jacekpietras.zoo.domain.feature.pathfinder.model.Node
import com.jacekpietras.zoo.domain.feature.pathfinder.model.SnappedOn
import com.jacekpietras.zoo.domain.feature.pathfinder.model.SnappedOn.SnappedOnEdge
import kotlinx.coroutines.delay

internal class GraphAnalyzer {

    private var nodes: MutableSet<Node>? = null
    private val snapper = PointSnapper()

    fun initialize(roads: List<PathEntity>, technical: List<PathEntity>) {
        nodes = NodeSetFactory(roads, technical).create()
    }

    fun isInitialized(): Boolean = nodes != null

    suspend fun getTerminalPoints(): List<PointD> =
        waitForNodes()
            .asSequence()
            .filter { it.edges.size > 2 }
            .map(Node::point)
            .toList()

    suspend fun getSnappedPointOnEdge(
        point: PointD,
        technicalAllowed: Boolean,
    ): SnappedOnEdge =
        snapper.getSnappedOnEdge(
            waitForNodes(),
            point,
            technicalAllowed
        )

    suspend fun getShortestPathWithContext(
        endPoint: SnappedOnEdge,
        startPoint: SnappedOnEdge,
        technicalAllowed: Boolean = false,
    ): List<Node> =
        getShortestPathJob(
            start = startPoint,
            end = endPoint,
            technicalAllowed = technicalAllowed,
        )

    internal suspend fun getShortestPath(
        endPoint: PointD,
        startPoint: PointD?,
        technicalAllowedAtStart: Boolean = true,
        technicalAllowedAtEnd: Boolean = false,
    ): List<PointD> {
        val nodes = waitForNodes()

        if (startPoint == null) return listOf(endPoint)
        if (nodes.isEmpty()) return listOf(endPoint)

        val snapStart = snapper.getSnappedOn(nodes, startPoint, technicalAllowed = technicalAllowedAtStart)
        val snapEnd = snapper.getSnappedOn(nodes, endPoint, technicalAllowed = technicalAllowedAtEnd)

        if (snapStart == snapEnd) return listOf(snapEnd.point)

        return getShortestPathJob(
            start = snapStart,
            end = snapEnd,
            technicalAllowed = technicalAllowedAtEnd,
        ).map(Node::point)
    }

    private suspend fun getShortestPathJob(
        start: SnappedOn,
        end: SnappedOn,
        technicalAllowed: Boolean = false,
    ): List<Node> =
        Dijkstra(
            vertices = waitForNodes(),
            technicalAllowed = technicalAllowed,
        ).calculate(
            start = start,
            end = end,
        )

    internal suspend fun waitForNodes(): MutableSet<Node> {
        while (nodes == null) {
            delay(100)
        }
        return checkNotNull(nodes)
    }
}